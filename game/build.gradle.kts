import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.23" apply false
    id("com.github.johnrengelman.shadow").version("8.1.1") apply true
    id("org.jetbrains.compose") version "1.6.1" apply true
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.dejvokep:boosted-yaml:1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(compose.material3)
    implementation(project(":core"))
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.0")
}

compose.desktop {
    application {
        mainClass = "me.codedbyyou.os.game.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "OSGameTwo"
            packageVersion = "1.0.0"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        archiveFileName.set("OSGameStable.jar")
        manifest {
            attributes(
                "Main-Class" to "me.codedbyyou.os.game.MainKt"
            )
        }

        if (System.getProperty("os.name").contains("Windows", ignoreCase = true))
            destinationDirectory.set(file("C:\\Users\\AFK\\Desktop"))
    }
}

tasks.register<Jar>("fatJar") {
    manifest {
        attributes["Main-Class"] = "me.codedbyyou.os.game.MainKt"
//        attributes["Icon-File"] = "resources/logo.svg"
    }
    from(sourceSets.main.get().output)
    archiveClassifier.set("fat")
}

tasks {
    build {
        dependsOn("fatJar", shadowJar)
    }
}