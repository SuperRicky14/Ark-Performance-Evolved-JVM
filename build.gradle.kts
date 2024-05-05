plugins {
    id("java")
    kotlin("jvm")
}

group = "net.superricky"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))
    implementation("net.java.dev.jna:jna-platform:5.6.0")
    implementation("org.json:json:20240303")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}