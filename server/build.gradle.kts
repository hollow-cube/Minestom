plugins {
    kotlin("jvm") version "1.9.10"
}

group = "net.bytemc"
version = "1.0.0-SNAPSHIT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(mapOf("path" to ":")))
}

kotlin {
    jvmToolchain(17)
}