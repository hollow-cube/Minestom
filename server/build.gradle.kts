plugins {
    kotlin("jvm") version "1.9.20"
    id("com.jfrog.artifactory") version "5.1.10"
}

group = "net.bytemc"
version = "1.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.20")
    implementation(project(mapOf("path" to ":")))
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "net.bytemc.minestom.server.ByteServerBootstrap"
    }
}

kotlin {
    jvmToolchain(17)
}