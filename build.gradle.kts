import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.9.23" apply false
    kotlin("plugin.serialization") version "1.9.23" apply false
    kotlin("multiplatform") version "1.9.23" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("org.jetbrains.compose") version "1.6.1" apply false
}

group = "me.codedbyyou.os"
version = "1.0-SNAPSHOT"


allprojects {
    group = "me.codedbyyou.os"
    description = "OSGame for Operating Systems Course"
    version = "1.0-SNAPSHOT"


    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

}

//project(":compose") {
//    apply(plugin = "org.jetbrains.compose")
//    apply(plugin = "kotlin")
//    tasks.withType<KotlinCompile> {
//        kotlinOptions {
//            jvmTarget = "18"
//            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
//        }
//    }
//
//    configure<JavaPluginExtension> {
//        targetCompatibility = JavaVersion.VERSION_18
//    }
//}

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
        targetCompatibility = if (project.name != "compose") {
            JavaVersion.VERSION_1_8
        }else{
            JavaVersion.VERSION_18
        }
    }

    afterEvaluate{
        tasks.findByName("shadowJar")?.also {
            tasks.named("assemble") { dependsOn(it) }
        }
    }
}

dependencies {
//    testImplementation(kotlin("test"))
//    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}