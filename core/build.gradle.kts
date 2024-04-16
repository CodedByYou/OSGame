plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("dev.dejvokep:boosted-yaml:1.3")
}


kotlin {
    jvmToolchain(8)
}