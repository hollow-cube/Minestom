plugins {
    kotlin("jvm") version "1.9.20"
}

group = "net.bytemc"
version = "1.0.0-SNAPSHIT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(17)
}