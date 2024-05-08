import org.apache.tools.ant.taskdefs.condition.Os
import com.lehaine.littlekt.gradle.texturepacker.littleKt
import com.lehaine.littlekt.gradle.texturepacker.texturePacker
import org.gradle.kotlin.dsl.version

buildscript {
    val littleKtVersion = "0.9.0" // or whichever hash you are using
    val kotlinCoroutinesVersion = "1.8.0" // or whatever version you are using

    repositories {
        gradlePluginPortal()
        google()
        mavenLocal()
        mavenCentral()
        maven(url ="https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
    dependencies {
        classpath("com.lehaine.littlekt.gradle:texturepacker:$littleKtVersion")
    }
}

plugins {
    kotlin("multiplatform")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "1.9.23" apply false
    id("com.lehaine.littlekt.gradle.texturepacker") version "0.9.0"
    application
}

//project.setProperty("mainClassName", "me.codedbyyou.os.client.MainKt")

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven(url ="https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

littleKt {
    texturePacker {
        inputDir = "assets"
        outputDir = "src/main/resources/assets"
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("dev.dejvokep:boosted-yaml:1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}


tasks.test {
    useJUnitPlatform()
}


kotlin {
    jvm {
        sourceSets {
            val jvmMain by getting {
                dependencies {
                    implementation("com.lehaine.littlekt:core:0.9.0")
                    implementation(project(":core"))
                    implementation(kotlin("stdlib-jdk8"))
                    implementation("dev.dejvokep:boosted-yaml:1.3")
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                }
            }
        }
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        compilations {
            val main by getting

            val mainClassName = "me.codedbyyou.os.client.MainKt"
            tasks {
                register<Copy>("copyResources") {
                    group = "package"
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    dependsOn(named("jvmProcessResources"))
                    from(main.output.resourcesDir)
                    destinationDir = File("${layout.buildDirectory.asFile.get()}/publish")
                }
                register<Jar>("packageFatJar") {
                    group = "package"
                    archiveClassifier.set("all")
                    manifest {
                        attributes(
                            "Main-Class" to "me.codedbyyou.os.client.MainKt"
                        )
                    }
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    dependsOn(named("jvmJar"))
                    dependsOn(named("copyResources"))
                    destinationDirectory.set(File("${layout.buildDirectory.asFile.get()}/publish/"))
                    from(
                        main.runtimeDependencyFiles.map { if (it.isDirectory) it else zipTree(it) },
                        main.output.classesDirs
                    )
                    doLast {
                        project.logger.lifecycle("[LittleKt] The packaged jar is available at: ${outputs.files.first().parent}")
                    }
                }
                // workaround for https://youtrack.jetbrains.com/issue/KT-62214
                if (Os.isFamily(Os.FAMILY_MAC) && mainClassName != null) {
                    register<JavaExec>("jvmRun") {
                        jvmArgs("-XstartOnFirstThread")
                        mainClass.set(mainClassName)
                        kotlin {
                            val mainCompile = targets["jvm"].compilations["main"]
                            dependsOn(mainCompile.compileAllTaskName)
                            classpath(
                                { mainCompile.output.allOutputs.files },
                                (configurations["jvmRuntimeClasspath"])
                            )
                        }
                    }
                }
            }
        }

    }

    application {
        mainClass.set("me.codedbyyou.os.client.MainKt")
    }
//}

    tasks {

        jar {
            manifest {
                attributes(
                    "Main-Class" to "me.codedbyyou.os.client.MainKt"
                )
            }
        }

        shadowJar {
            archiveBaseName.set("OSGameClient")
            archiveVersion.set("1.0-SNAPSHOT")
            manifest {
                attributes(
                    "Main-Class" to "me.codedbyyou.os.client.MainKt"
                )
            }

        }
    }
}