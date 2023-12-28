plugins {
    kotlin("jvm") version "1.9.22"
    id("com.jfrog.artifactory") version "5.1.11"
    id ("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.bytemc"

repositories {
    mavenCentral()
}

dependencies {
    api(project(mapOf("path" to ":")))
    api("net.kyori:adventure-text-minimessage:4.14.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "net.bytemc.minestom.server.ByteServerBootstrap"
    }
}

kotlin {
    jvmToolchain(17)
}