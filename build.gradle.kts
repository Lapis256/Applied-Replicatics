
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.modmuss50.mpp.ReleaseType
import net.neoforged.moddev.shadow.org.apache.maven.artifact.versioning.DefaultArtifactVersion
import net.neoforged.moddevgradle.dsl.InternalModelHelper
import net.neoforged.moddevgradle.dsl.RunModel
import net.neoforged.moddevgradle.internal.RunGameTask
import org.apache.tools.ant.filters.ReplaceTokens
import org.ec4j.core.Resource
import org.ec4j.core.ResourcePropertiesService
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.slf4j.event.Level
import java.text.SimpleDateFormat
import java.util.Date


buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.ec4j)
    }
}

plugins {
    id("java")
    id("java-library")
    id("idea")
    id("maven-publish")

    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.moddev)
    alias(libs.plugins.spotless)
    alias(libs.plugins.modPublishPlugin)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
}

spotless {
    encoding("UTF-8")
    lineEndings = com.diffplug.spotless.LineEnding.UNIX
    java {
        target("src/**/*.java")
        val editorConfig =
            ResourcePropertiesService
                .builder()
                .build()
                .queryProperties(
                    Resource.Resources.ofPath(file("src/main/java/Formatting.java").toPath(), Charsets.UTF_8),
                ).properties
        eclipse("4.41").configProperties(
            """
            org.eclipse.jdt.core.formatter.tabulation.char=${editorConfig.getValue("indent_style").sourceValue}
            org.eclipse.jdt.core.formatter.tabulation.size=${editorConfig.getValue("indent_size").sourceValue}
            org.eclipse.jdt.core.formatter.indentation.size=${editorConfig.getValue("indent_size").sourceValue}
            org.eclipse.jdt.core.formatter.lineSplit=${editorConfig.getValue("max_line_length").sourceValue}
            """.trimIndent(),
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlin {
        target("src/**/*.kt", "buildSrc/src/**/*.kt")
        ktlint(libs.versions.ktlint.get()).setEditorConfigPath(rootProject.file(".editorconfig"))
    }
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/*.gradle.kts")
        ktlint(libs.versions.ktlint.get()).setEditorConfigPath(rootProject.file(".editorconfig"))
    }
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
    maven("https://modmaven.dev") {
        content { includeGroup("com.hrznstudio") }
    }
    maven("https://maven.blamejared.com") {
        content { includeGroup("mezz.jei") }
    }
    maven("https://maven.terraformersmc.com/releases") {
        content { includeGroup("dev.emi") }
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "Curse Maven"
                url = uri("https://cursemaven.com")
            }
        }
        filter { includeGroup("curse.maven") }
    }
    mavenCentral()
}

val generateModMetadata by tasks.registering(ProcessResources::class)

val apiSourceSet by sourceSets.register("api")

sourceSets.test {
    compileClasspath += apiSourceSet.output
    runtimeClasspath += apiSourceSet.output
}

val mainSourceSet by sourceSets.named("main") {
    compileClasspath += apiSourceSet.output
    runtimeClasspath += apiSourceSet.output

    resources {
        srcDirs(
            "src/generated/resources",
            generateModMetadata.get().outputs.files,
        )
        exclude("**/.cache")
    }
}

val gameTestSourceSet by sourceSets.register("gameTest") {
    compileClasspath += mainSourceSet.output + mainSourceSet.compileClasspath
    runtimeClasspath += mainSourceSet.output + apiSourceSet.output
}

configurations[gameTestSourceSet.implementationConfigurationName].extendsFrom(configurations.implementation.get(), configurations.runtimeOnly.get())
configurations[gameTestSourceSet.runtimeClasspathConfigurationName].exclude(group = "mezz.jei")

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

obfuscation {
    createRemappingConfiguration(configurations[integrationSourceSet.implementationConfigurationName])
    createRemappingConfiguration(configurations[integrationSourceSet.compileOnlyConfigurationName])
    createRemappingConfiguration(configurations[optionalModsSourceSet.runtimeOnlyConfigurationName])
    createRemappingConfiguration(configurations[dataSourceSet.runtimeOnlyConfigurationName])
    createRemappingConfiguration(configurations[dataSourceSet.implementationConfigurationName])
}

dependencies {
    // バンドルの名前から依存関係を追加する。
    val catalog = project.versionCatalogs.named("libs")
    catalog.bundleAliases.forEach {
        val bundle = catalog.findBundle(it).orElse(null) ?: return@forEach
        val configuration = configurations.named(it).orNull ?: return@forEach
        configuration(bundle)
    }

    implementation(kotlin("reflect"))
    jarJar(libs.mixinextras.forge.get())
    annotationProcessor(variantOf(libs.mixin, "processor"))

    configurations[apiSourceSet.implementationConfigurationName].extendsFrom(configurations.implementation.get())
    "modOptionalModsRuntimeOnly"(libs.bundles.modIntegrationImplementation)
    "modDataRuntimeOnly"(libs.bundles.modIntegrationImplementation)

    dokkaHtmlPlugin(libs.dokka.versioning)
}

legacyForge {
    version = "$mcVersion-${libs.versions.forge.get()}"

    validateAccessTransformers = true

    addModdingDependenciesTo(apiSourceSet)
    addModdingDependenciesTo(integrationSourceSet)
    addModdingDependenciesTo(gameTestSourceSet)

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
        val mainResources = file("src/main/resources/").absolutePath

        fun RunModel.setupGuideMELivePreview() {
            systemProperty("guideme.ae2.guide.sources", "$mainResources/assets/$modId/ae2guide")
            systemProperty("guideme.ae2.guide.sourcesNamespace", modId)
            jvmArgument("--add-modules=jdk.incubator.vector")
        }

        register("client") {
            client()
            gameDirectory.set(rootProject.file("run"))

            setupGuideMELivePreview()
        }

        register("clientWithOptionalMods") {
            client()
            gameDirectory.set(rootProject.file("run"))

            sourceSet = optionalModsSourceSet

            setupGuideMELivePreview()
        }

        register("server") {
            server()
            gameDirectory.set(rootProject.file("run-server"))
        }

        register("gameTestServer") {
            type.set("gameTestServer")
            sourceSet = gameTestSourceSet
            gameDirectory.set(rootProject.file("build/run-gametest"))
        }

        register("data") {
            data()
            sourceSet = dataSourceSet
            gameDirectory.set(rootProject.file("run-data"))
            programArguments.addAll(
                "--mod",
                modId,
                "--all",
                "--output",
                file("src/generated/resources/").absolutePath,
                "--existing",
                mainResources,
                "--existing-mod",
                "ae2",
            )
        }

        configureEach {
            dependencies { add(additionalRuntimeClasspathConfiguration.name, kotlin("reflect")) }
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = Level.DEBUG
            disableIdeRun()

            if (!type.get().lowercase().endsWith("data")) {
                systemProperty("forge.enabledGameTestNamespaces", modId)
                systemProperty("mixin.debug.export", "true")
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
            sourceSet(gameTestSourceSet)
        }
    }

    ideSyncTask(generateModMetadata)
}

mixin {
    add(mainSourceSet, "$modId.refmap.json")
    config("$modId.mixins.json")
}

val modDependencies =
    listOf(
        ModDep("forge", libs.versions.forge..<"48"),
        ModDep("minecraft", mcVersion.eq()),
        ModDep("ae2", libs.versions.ae2Mod.gte(), ordering = Order.AFTER),
        ModDep("replication", libs.versions.replicationMod.gte(), ordering = Order.AFTER),
        ModDep.optional("megacells", libs.versions.megaCellsMod.gte(), ordering = Order.AFTER),
        ModDep.optional("appflux", libs.versions.appliedFluxMod.gte(), ordering = Order.AFTER),
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
        val javaCompilerMetadata =
            project.javaToolchains
                .compilerFor(java.toolchain)
                .get()
                .metadata

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

    val latestDocVersion =
        docDir
            .resolve("version.json")
            .takeIf(File::exists)
            ?.readText()
            ?.let {
                Json
                    .parseToJsonElement(it)
                    .jsonObject["version"]
                    ?.jsonPrimitive
                    ?.contentOrNull
            }?.trim()
            ?: currentVersion

    val orderedVersions =
        docVersionDir
            .listFiles(File::isDirectory)
            ?.asSequence()
            ?.map(File::getName)
            ?.plus(latestDocVersion)
            ?.plus(currentVersion)
            ?.map(::DefaultArtifactVersion)
            ?.sortedDescending()
            ?.map(DefaultArtifactVersion::toString)
            ?.toSet()
            ?.toList()
            ?: emptyList()

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
        manifest.attributes("MixinConfigs" to "$modId.mixins.json")
        from(apiSourceSet.output, integrationSourceSet.output)
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
            integrationSourceSet.allSource,
        )
    }

    withType<RunGameTask>().configureEach {
        javaLauncher.set(
            project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(jdkVersion))
                vendor.set(jvmVendor)
            },
        )
        standardInput = System.`in`
    }

    withType<Jar>().configureEach {
        from(rootProject.file("LICENSE")) {
            rename { "LICENSE_${Constants.Mod.ID}" }
        }

        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }

    generateModMetadata {
        val replaceProperties: MutableMap<String, String> =
            mutableMapOf(
                "version" to Constants.Mod.VERSION,
                "group" to Constants.Mod.GROUP,
                "minecraft_version" to mcVersion,
                "mod_loader" to "javafml",
                "mod_loader_version_range" to "[47,48)",
                "mod_name" to Constants.Mod.NAME,
                "mod_author" to Constants.Mod.AUTHOR,
                "mod_id" to modId,
                "license" to Constants.Mod.LICENSE,
                "description" to Constants.Mod.DESCRIPTION,
                "display_url" to Constants.Mod.REPOSITORY_URL,
                "issue_tracker_url" to Constants.Mod.ISSUE_TRACKER_URL,
                "dependencies" to buildDeps(*modDependencies.toTypedArray(), modId = modId),
            )

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
    type = ReleaseType.STABLE
    modLoaders.add("forge")

    curseforge {
        requires("kotlinlangforge")
        requires("applied-energistics-2")
        requires("replication")
        optional("mega-cells")
        optional("applied-flux")

        minecraftVersions.add(mcVersion)
        clientRequired = true
        serverRequired = true

        projectId = Constants.Publisher.CURSEFORGE_PROJECT_ID
        accessToken = System.getenv("CURSEFORGE_TOKEN")
        javaVersions.add(JavaVersion.toVersion(jdkVersion))
        changelogType = "markdown"
    }

    modrinth {
        requires("kotlin-lang-forge")
        requires("ae2")
        requires("replication")
        optional("mega")
        optional("appflux")

        minecraftVersions.add(mcVersion)

        projectId = Constants.Publisher.MODRINTH_PROJECT_ID
        accessToken = System.getenv("MODRINTH_TOKEN")
    }

    val releaseFilesDir = providers.gradleProperty("releaseFilesDir").orElse("dist")
    val releaseDirFileProvider = releaseFilesDir.map { layout.projectDirectory.dir(it).asFile }
    val (mainJar, otherJars) =
        releaseDirFileProvider.pickJars(
            "${Constants.Mod.PROJECT_NAME}-${Constants.Mod.VERSION}",
            "sources",
            "javadoc",
            "api",
        )

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
