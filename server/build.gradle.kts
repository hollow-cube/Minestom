plugins {
    kotlin("jvm") version "1.9.21"
    id("com.jfrog.artifactory") version "5.1.11"
    id ("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.bytemc"
version = "1.3.6-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.21")
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

publishing {
    publications {
        create<MavenPublication>("mavenJavaServer") {
            from(components["java"])

            this.groupId = project.group.toString()
            this.artifactId = project.name
            this.version = project.version.toString()
        }
    }

    repositories {
        maven {
            name = "bytemc"
            url = uri("https://artifactory.bytemc.de/artifactory/bytemc-public/")
            credentials {
                username = System.getenv("BYTEMC_REPO_USERNAME")
                password = System.getenv("BYTEMC_REPO_PASSWORD")
            }
        }
    }
}