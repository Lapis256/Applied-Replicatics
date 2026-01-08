import org.gradle.jvm.toolchain.JvmVendorSpec


object Constants {
    object Mod {
        const val ID = "apprep"
        const val NAME = "Applied Replicatics"
        const val PROJECT_NAME = "Applied-Replicatics"
        const val DESCRIPTION = "DESCRIPTION"
        const val LICENSE = "LGPL-3.0"
        const val VERSION = "21.1-1.0.0"
        const val GROUP = "dev.lapis256"
        const val AUTHOR = "Lapis256"
        const val REPOSITORY_URL = "https://github.com/Lapis256/Applied-Replicatics"
        const val ISSUE_TRACKER_URL = "$REPOSITORY_URL/issues"
    }

    object Publisher {
        const val CURSEFORGE_PROJECT_ID = ""
        const val MODRINTH_PROJECT_ID = ""
    }

    object Dev {
        const val JDK_VERSION = 21
        @Suppress("UnstableApiUsage")
        val JVM_VENDOR: JvmVendorSpec = JvmVendorSpec.JETBRAINS
    }
}
