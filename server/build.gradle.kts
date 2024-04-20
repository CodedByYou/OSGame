import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "1.9.23" apply false
    application
}

project.setProperty("mainClassName", "me.codedbyyou.os.server.MainKt")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(kotlin("test"))
    implementation(project(":core"))
    implementation("dev.dejvokep:boosted-yaml:1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.reflections:reflections:0.9.12")
//    implementation("javax.swing:javax.swing-api:1.0.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

application {
    mainClass.set("me.codedbyyou.os.server.MainKt")
}

tasks {

    jar {
        manifest {
            attributes(
                "Main-Class" to "me.codedbyyou.os.server.MainKt"
            )
        }
    }

    shadowJar {
        archiveBaseName.set("OSGameServer")
        archiveClassifier.set("")
        archiveVersion.set("1.0-SNAPSHOT")
        relocate("dev.dejvokep.boostedyaml", "me.codedbyyou.os.libs.boostedyaml")
        manifest {
            attributes(
                "Main-Class" to "me.codedbyyou.os.server.MainKt"
            )
        }
    }
}