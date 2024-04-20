plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.23" apply false
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("dev.dejvokep:boosted-yaml:1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}


kotlin {
    jvmToolchain(8)
}