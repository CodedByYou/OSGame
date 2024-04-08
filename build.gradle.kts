import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.9.21" apply false
    kotlin("plugin.serialization") version "1.9.21" apply false
    kotlin("multiplatform") version "1.9.21" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

group = "me.codedbyyou.os"
version = "1.0-SNAPSHOT"


allprojects {
    group = "me.codedbyyou.os"
    description = "OSGame for Operating Systems Course"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

}

subprojects {
    apply(plugin = "java")
//    apply(plugin = "kotlin")

//    tasks.withType<KotlinCompile> {
//        kotlinOptions {
//            jvmTarget = "1.8"
//            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
//        }
//    }

    configure<JavaPluginExtension> {
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    afterEvaluate{
        tasks.findByName("shadowJar")?.also {
            tasks.named("assemble") { dependsOn(it) }
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
//    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}