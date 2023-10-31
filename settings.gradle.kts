pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.20"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "minestom-ce"
include("server")
