plugins {
    id("java")
}

group = "net.bytemc.jumpleague"
version = "1.3.10-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(project(mapOf("path" to ":")))
}

tasks.test {
    useJUnitPlatform()
}