import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.jetbrains.intellij") version "0.4.22"
    java
    kotlin("jvm") version  "1.4.30"
    kotlin("plugin.serialization") version  "1.4.30"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.openjfx.javafxplugin") version "0.0.8"
    id("com.gluonhq.client-gradle-plugin") version "0.0.11"
    id("org.jetbrains.dokka") version "0.10.1"
}

group = "org.jetbrains.research.ml.tasktracker"
version = "1.1"

repositories {
    maven (url = "https://www.jetbrains.com/intellij-repository/releases")
    maven (url = "https://jetbrains.bintray.com/intellij-third-party-dependencies")
    maven (url = "https://nexus.gluonhq.com/nexus/content/repositories/releases/")
    maven (url = "https://jitpack.io")

    mavenCentral()
    jcenter()
    google()
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0-M1-1.4.0-rc-218")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.20")
    implementation("com.opencsv","opencsv", "5.0")
    implementation("joda-time", "joda-time", "2.9.2")
    implementation("org.apache.commons", "commons-csv", "1.7")
    // https://mvnrepository.com/artifact/com.gluonhq/charm-glisten
    implementation("com.gluonhq", "charm-glisten", "6.0.1")
    implementation("com.google.code.gson", "gson", "2.8.5")
    implementation("com.squareup.okhttp3", "okhttp", "4.2.2")
//    implementation("org.controlsfx:controlsfx:11.0.3")
    compile("com.google.auto.service:auto-service:1.0-rc7")
    implementation("org.eclipse.mylyn.github", "org.eclipse.egit.github.core", "2.1.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:0.9.1")
    // https://mvnrepository.com/artifact/net.lingala.zip4j/zip4j (used for unzipping required plugins)
    implementation("net.lingala.zip4j", "zip4j", "2.6.1")
    implementation("com.github.holgerbrandl:krangl:v0.13")

    testCompile("junit", "junit", "4.12")

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

/*
   Uncomment for testing with Rider IDE
*/
//tasks.getByName<org.jetbrains.intellij.tasks.IntelliJInstrumentCodeTask>("instrumentCode") {
//    setCompilerVersion("192.6817.32")
//}
//intellij {
//    type = "RD"
//    version = "2019.2-SNAPSHOT"
//    downloadSources = false
//    intellij.updateSinceUntilBuild = false
//}


/*
   Uncomment for testing with Intellij IDEA
*/
//intellij {
//    version = "2020.3"
//}


/*
   Uncomment for testing with PyCharm IDE
*/


intellij {
    type = "PC"
    version = "2020.1"
    setPlugins("PythonCore")

    pluginName = "code-tracker-plugin"
    downloadSources = true
    sameSinceUntilBuild = false
    updateSinceUntilBuild = false
}

javafx {
    version = "15"
    modules("javafx.controls", "javafx.fxml", "javafx.swing")
    configuration = "compileOnly"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      Add change notes here.<br>
      <em>most HTML tags may be used</em>""")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

gluonClient {
    reflectionList = arrayListOf("javafx.fxml.FXMLLoader", "com.gluon.hello.views.HelloPresenter",
        "javafx.scene.control.Button", "javafx.scene.control.Label")
}
tasks.withType<ShadowJar> {
    project.logger.warn("Don't forget to:\n" +
            "- set your remote server as baseUrl in QueryExecutor class\n" +
            "- turn OFF org.jetbrains.research.ml.tasktracker.Plugin.testMode")
}

tasks.withType<Wrapper> {
    gradleVersion = "6.8"
}