import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.neoforged.moddev.shadow.org.apache.maven.artifact.versioning.DefaultArtifactVersion
import net.neoforged.moddevgradle.dsl.InternalModelHelper
import net.neoforged.moddevgradle.internal.RunGameTask
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.slf4j.event.Level
import java.text.SimpleDateFormat
import java.util.*


plugins {
    id("java")
    id("java-library")
    id("idea")
    id("maven-publish")

    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.moddev)
    alias(libs.plugins.modPublishPlugin)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
}

val modId = Constants.Mod.ID
val mcVersion: String = libs.versions.minecraft.get()

val jdkVersion = Constants.Dev.JDK_VERSION
val jvmVendor = Constants.Dev.JVM_VENDOR


base {
    archivesName = Constants.Mod.PROJECT_NAME
    version = Constants.Mod.VERSION
    group = Constants.Mod.GROUP
}

repositories {
    mavenLocal()
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
    maven {
        name = "JEI"
        url = uri("https://modmaven.dev/")
    }
    maven {
        name = "Curse Maven"
        url = uri("https://cursemaven.com")
    }
    mavenCentral()
}

val generateModMetadata by tasks.registering(ProcessResources::class)

val apiSourceSet by sourceSets.register("api")

val mainSourceSet by sourceSets.named("main") {
    compileClasspath += apiSourceSet.output
    runtimeClasspath += apiSourceSet.output

    resources {
        srcDirs(
            "src/generated/resources",
            generateModMetadata.get().outputs.files
        )
        exclude("**/.cache")
    }
}

val optionalModsSourceSet by sourceSets.register("optionalMods") {
    compileClasspath += mainSourceSet.output + mainSourceSet.compileClasspath
    runtimeClasspath += mainSourceSet.output + mainSourceSet.runtimeClasspath
}

val integrationSourceSet by sourceSets.register("integration") {
    compileClasspath += mainSourceSet.output + mainSourceSet.compileClasspath
    runtimeClasspath += mainSourceSet.output + mainSourceSet.runtimeClasspath
}

val dataSourceSet by sourceSets.register("data") {
    compileClasspath += integrationSourceSet.output + integrationSourceSet.compileClasspath
    runtimeClasspath += integrationSourceSet.output + integrationSourceSet.runtimeClasspath
}

dependencies {
    // バンドルの名前から依存関係を追加する。
    val catalog = project.versionCatalogs.named("libs")
    catalog.bundleAliases.forEach {
        val bundle = catalog.findBundle(it).orElse(null) ?: return@forEach
        val configuration = configurations.named(it).orNull ?: return@forEach
        configuration(bundle)
    }

    "apiImplementation"(libs.bundles.implementation)
    "optionalModsRuntimeOnly"(libs.bundles.integrationImplementation)

    dokkaHtmlPlugin(libs.dokka.versioning)
}

neoForge {
    version = libs.versions.neoforge.get()

    validateAccessTransformers = true

    addModdingDependenciesTo(apiSourceSet)
    addModdingDependenciesTo(integrationSourceSet)

    accessTransformers {
        val files = rootProject.fileTree("src").matching { include("*/resources/META-INF/accesstransformer.cfg") }
        files.forEach { atFile ->
            println("adding access transformer file: $atFile")

            from(atFile)
            publish(atFile)
        }
    }

    parchment {
        mappingsVersion = libs.versions.parchmentmc.get()
        minecraftVersion = mcVersion
    }

    runs {
        register("client") {
            client()
            gameDirectory.set(rootProject.file("run"))
        }

        register("clientWithOptionalMods") {
            client()
            gameDirectory.set(rootProject.file("run"))

            sourceSet = optionalModsSourceSet
        }

        register("server") {
            server()
            gameDirectory.set(rootProject.file("run-server"))
        }

        register("data") {
            data()
            sourceSet = dataSourceSet
            gameDirectory.set(rootProject.file("run-data"))
            programArguments.addAll(
                "--mod",
                modId,
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath,
                "--existing-mod", "ae2",
            )
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = Level.DEBUG
            disableIdeRun()

            if (!type.get().lowercase().endsWith("data")) {
                systemProperty("neoforge.enabledGameTestNamespaces", modId)
                systemProperty("mixin.debug.export", "true")
                jvmArgument("-XX:+AllowEnhancedClassRedefinition")
            }

            if (type.get().startsWith("server")) {
                programArgument("--nogui")

                // サーバー実行タスクの実行前に run-server-templates から初期ファイルをコピーする
                @Suppress("UnstableApiUsage")
                tasks.named(InternalModelHelper.nameOfRun(this, "prepare", "run")) {
                    val templatesDir = layout.projectDirectory.dir("run-server-templates")
                    inputs.dir(templatesDir)

                    val outputDir = gameDirectory

                    // ファイルがすべて存在する場合は実行済みとみなす
                    outputs.upToDateWhen {
                        val srcRoot = templatesDir.asFile.toPath()
                        val templateFiles = templatesDir.asFileTree.files.filter { it.isFile }
                        templateFiles.all { src ->
                            val rel = srcRoot.relativize(src.toPath()).toString()
                            val outFile = outputDir.map { it.file(rel).asFile.exists() }
                            outFile.get()
                        }
                    }

                    doLast {
                        templatesDir.asFile.copyRecursively(outputDir.get().asFile, overwrite = false) { _, _ -> OnErrorAction.SKIP }
                    }
                }
            }
        }
    }

    mods {
        register(modId) {
            sourceSet(mainSourceSet)
            sourceSet(apiSourceSet)
            sourceSet(integrationSourceSet)
            sourceSet(dataSourceSet)
        }
    }

    unitTest {
        enable()

        testedMod = mods[modId]
    }

    ideSyncTask(generateModMetadata)
}

val mixin = arrayOf(
    "$modId.mixins.json"
)

val modDependencies = listOf(
    ModDep("neoforge", libs.versions.neoforge..<"21.2"),
    ModDep("minecraft", mcVersion.eq()),
    ModDep("kotlinforforge", libs.versions.kotlinForForge.gte()),
    ModDep("ae2", libs.versions.ae2.gte()),
    ModDep("replication", "1.21.1-1.2.6".gte()),
    ModDep.optional("megacells", "4.10.1".gte()),
)

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(jdkVersion)
        vendor = jvmVendor
    }
    JavaVersion.toVersion(jdkVersion).let {
        sourceCompatibility = it
        targetCompatibility = it
    }
}

kotlin {
    jvmToolchain(jdkVersion)

    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("$jdkVersion")
        freeCompilerArgs.addAll()
    }
}

fun Jar.setJarManifest() {
    manifest {
        val javaCompilerMetadata = project.javaToolchains.compilerFor(java.toolchain).get().metadata

        @OptIn(ExperimentalBuildToolsApi::class, ExperimentalKotlinGradlePluginApi::class)
        val kotlinCompilerVersion = kotlin.compilerVersion.get()

        attributes(
            "Specification-Title" to Constants.Mod.NAME,
            "Specification-Vendor" to Constants.Mod.AUTHOR,
            "Specification-Version" to version,
            "Implementation-Title" to Constants.Mod.PROJECT_NAME,
            "Implementation-Version" to version,
            "Implementation-Vendor" to Constants.Mod.AUTHOR,
            "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
            "Timestamp" to System.currentTimeMillis(),
            "Built-On-Java" to "${javaCompilerMetadata.jvmVersion} (${javaCompilerMetadata.vendor})",
            "Built-On-Kotlin" to kotlinCompilerVersion,
            "Built-On-Minecraft" to mcVersion,
        )
    }
}

dokka {
    dokkaSourceSets {
        listOf("main", "api", "integration").forEach {
            named(it) {
                sourceRoots.from(file("src/$it"))
                suppress.set(false)
            }
        }

        configureEach {
            displayName = "jvm"
            moduleName = Constants.Mod.PROJECT_NAME
            moduleVersion = Constants.Mod.VERSION
            skipEmptyPackages = true
            skipDeprecated = false

            documentedVisibilities.add(VisibilityModifier.Public)

            enableJdkDocumentationLink = true
            enableKotlinStdLibDocumentationLink = true
            enableAndroidDocumentationLink = false

            sourceLink {
                remoteUrl = uri(Constants.Mod.REPOSITORY_URL)
                remoteLineSuffix = "#L"
            }
        }
    }

    val rootDir = projectDir.resolve("documentation")
    val docDir = rootDir.resolve("docs")
    val docVersionDir = rootDir.resolve("version")
    docVersionDir.mkdirs()

    val currentVersion = Constants.Mod.VERSION

    dokkaPublications.html {
        outputDirectory.set(docDir)
    }

    val latestDocVersion = docDir.resolve("version.json")
        .takeIf(File::exists)
        ?.readText()
        ?.let { Json.parseToJsonElement(it).jsonObject["version"]?.jsonPrimitive?.contentOrNull }
        ?.trim()
        ?: currentVersion

    val orderedVersions = docVersionDir.listFiles(File::isDirectory)
        .asSequence()
        .map(File::getName)
        .plus(latestDocVersion)
        .plus(currentVersion)
        .map(::DefaultArtifactVersion)
        .sortedDescending()
        .map(DefaultArtifactVersion::toString)
        .toSet()
        .toList()

    pluginsConfiguration {
        versioning {
            version = currentVersion
            versionsOrdering = orderedVersions
            olderVersionsDir = docVersionDir
            renderVersionsNavigationOnAllPages = true
        }
    }

    val copyLatestDoc by tasks.registering(Copy::class) {
        enabled = currentVersion != latestDocVersion
        from(docDir)
        into(docVersionDir.resolve(latestDocVersion))
        exclude("older")
    }

    tasks.dokkaGeneratePublicationHtml {
        dependsOn(copyLatestDoc)
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = jdkVersion
    }

    processResources {
        dependsOn(generateModMetadata)
    }

    jar {
        setJarManifest()
        from(mainSourceSet.output, apiSourceSet.output, integrationSourceSet.output)
    }

    val apiJar by registering(Jar::class) {
        group = "build"

        setJarManifest()
        dependsOn("apiClasses")
        archiveClassifier.set("api")
        from(apiSourceSet.output)
    }

    named<Jar>("sourcesJar") {
        dependsOn(classes, "apiClasses", "integrationClasses")

        from(
            mainSourceSet.allSource,
            apiSourceSet.allSource,
            integrationSourceSet.allSource
        )
    }

    withType<RunGameTask>().configureEach {
        javaLauncher.set(project.javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(jdkVersion))
            vendor.set(jvmVendor)
        })
        standardInput = System.`in`
    }

    withType<Jar>().configureEach {
        from(rootProject.file("LICENSE")) {
            rename { "LICENSE_${Constants.Mod.ID}" }
        }

        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }

    generateModMetadata {
        val replaceProperties: MutableMap<String, String> = mutableMapOf(
            "version" to Constants.Mod.VERSION,
            "group" to Constants.Mod.GROUP,
            "minecraft_version" to mcVersion,
            "mod_loader" to "javafml",
            "mod_loader_version_range" to "1",
            "mod_name" to Constants.Mod.NAME,
            "mod_author" to Constants.Mod.AUTHOR,
            "mod_id" to modId,
            "license" to Constants.Mod.LICENSE,
            "description" to Constants.Mod.DESCRIPTION,
            "display_url" to Constants.Mod.REPOSITORY_URL,
            "issue_tracker_url" to Constants.Mod.ISSUE_TRACKER_URL,
            "mixins" to "",
            "access_transformers" to "",
            "dependencies" to buildDeps(*modDependencies.toTypedArray(), modId = modId),
        )

        val atFiles = neoForge.accessTransformers.files
        if (!atFiles.isEmpty) {
            val atArrays = atFiles.joinToString("\n") { "[[accessTransformers]]\nfile = \"${it.name}\"" }
            replaceProperties["access_transformers"] = "\n$atArrays\n"
        }

        if (mixin.isNotEmpty()) {
            val mixinArrays = mixin.joinToString("\n") { "[[mixins]]\nconfig = \"$it\"" }
            replaceProperties["mixins"] = "\n$mixinArrays\n"
        }

        inputs.properties(replaceProperties)
        filter<ReplaceTokens>("beginToken" to $$"${", "endToken" to "}", "tokens" to replaceProperties)
        from(rootProject.file("src/templates"))
        into("build/generated/sources/$modId")
    }

    val releaseTag by registering(Exec::class) {
        group = "release"
        description = "Create an annotated git tag"

        val tag = "v${Constants.Mod.VERSION}"
        doFirst {
            commandLine("git", "tag", "-s", "-a", tag, "-m", "Release $tag")
        }
    }

    register<Exec>("pushReleaseTag") {
        group = "release"
        description = "Push the release tag to origin"
        dependsOn(releaseTag)

        val tag = "v${Constants.Mod.VERSION}"
        doFirst {
            commandLine("git", "push", "origin", tag)
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    val dokkaJavadocJar by registering(Jar::class) {
        description = "A Javadoc JAR containing Dokka Javadoc"
        from(dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
        archiveClassifier.set("javadoc")
    }

    build {
        dependsOn(apiJar, dokkaJavadocJar)
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

publishMods {
    type = ALPHA
    modLoaders.add("neoforge")

    curseforge {
        requires("applied-energistics-2")

        minecraftVersions.add(mcVersion)
        clientRequired = true
        serverRequired = true

        projectId = Constants.Publisher.CURSEFORGE_PROJECT_ID
        accessToken = System.getenv("CURSEFORGE_TOKEN")
        javaVersions.add(JavaVersion.toVersion(jdkVersion))
        changelogType = "markdown"
    }

    modrinth {
        requires("ae2")

        minecraftVersions.add(mcVersion)

        projectId = Constants.Publisher.MODRINTH_PROJECT_ID
        accessToken = System.getenv("MODRINTH_TOKEN")
    }

    val releaseFilesDir = providers.gradleProperty("releaseFilesDir").orElse("dist")
    val releaseDirFileProvider = releaseFilesDir.map { layout.projectDirectory.dir(it).asFile }
    val (mainJar, otherJars) = releaseDirFileProvider.pickJars("${Constants.Mod.PROJECT_NAME}-${Constants.Mod.VERSION}", "sources")

    file = mainJar
    additionalFiles.from(otherJars)
    dryRun = project.hasProperty("modPublishDryRun")
    changelog = System.getenv("CHANGELOG") ?: "No changelog provided"
    displayName = "[$mcVersion] v${Constants.Mod.VERSION}"
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            version = Constants.Mod.VERSION

            setArtifacts(listOf(tasks["jar"], tasks["sourcesJar"]))

            val atFile = layout.buildDirectory.file("copyAccessTransformersPublications/0-accesstransformer.cfg")
            if (atFile.get().asFile.exists()) {
                artifact(atFile) {
                    classifier = "accesstransformer"
                    extension = "cfg"
                }
            }
        }
    }
    repositories {
        mavenLocal()
    }
}
