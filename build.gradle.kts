import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.4.30"
    id("org.jetbrains.intellij") version "0.7.2"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.jetbrains.dokka") version "0.10.1"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    kotlin("plugin.serialization") version "1.4.30"
    id("org.openjfx.javafxplugin") version "0.0.9"
    id("com.gluonhq.client-gradle-plugin") version "0.1.38"
}

group = "org.jetbrains.research.ml.coding.assistant.ui"
version = "1.0"

repositories {
    maven(url = "https://www.jetbrains.com/intellij-repository/releases")
    maven(url = "https://jetbrains.bintray.com/intellij-third-party-dependencies")
    maven(url = "https://nexus.gluonhq.com/nexus/content/repositories/releases/")
    maven(url = "https://jitpack.io")

    mavenCentral()
    jcenter()
    google()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0-M1-1.4.0-rc-218")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.20")
    implementation("com.opencsv", "opencsv", "5.4")
    implementation("joda-time", "joda-time", "2.10.10")
    implementation("org.apache.commons", "commons-csv", "1.8")
    // https://mvnrepository.com/artifact/com.gluonhq/charm-glisten
    implementation("com.gluonhq", "charm-glisten", "6.0.1")
    implementation("com.google.code.gson", "gson", "2.8.6")
    implementation("com.squareup.okhttp3", "okhttp", "4.9.0")
    compile("com.google.auto.service:auto-service:1.0-rc7")
    implementation("org.eclipse.mylyn.github", "org.eclipse.egit.github.core", "2.1.5")
    implementation("net.lingala.zip4j", "zip4j", "2.7.0")
    implementation("com.github.holgerbrandl:krangl:v0.13")

    implementation("io.github.nbirillo.coding.assistant:coding-assistant-core") {
        version {
            branch = "rc-8"
        }
    }
    implementation("org.jetbrains.research.ml.ast.transformations:ast-transformations-core") {
        version {
            branch = "develop-8"
        }
    }
}

intellij {
    type = "PC"
    version = "2020.1"
    downloadSources = false
    setPlugins("PythonCore")
    updateSinceUntilBuild = true
}

ktlint {
    enableExperimentalRules.set(true)
}

javafx {
    version = "15"
    modules("javafx.controls", "javafx.fxml", "javafx.swing")
    configuration = "compileOnly"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}


tasks {
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<ShadowJar> {
        project.logger.warn(
            "Don't forget to:\n" +
                    "- set your remote server as baseUrl in QueryExecutor class\n" +
                    "- turn OFF org.jetbrains.research.ml.codingAssistant.Plugin.testMode"
        )
    }

    withType<Wrapper> {
        gradleVersion = "6.8"
    }
}

// According to this topic:
// https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010164960-Build-Intellij-plugin-in-IDEA-2019-1-2020-3?page=1#community_comment_360002517940
tasks.withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
    .forEach { it.enabled = false }
