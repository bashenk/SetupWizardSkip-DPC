plugins {
    base
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    kotlin("android") apply false
    //    kotlin("android.extensions") apply false
    kotlin("kapt") apply false
    id("net.csgstore.ssh") apply false
}

buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
        mavenCentral()
        google()
        jcenter()
        maven("https://maven.google.com")
        maven ("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://maven.pkg.github.com/")
        mavenLocal()
        gradlePluginPortal()
    }
    dependencies {
        classpath(group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin")
        classpath("com.android.tools.build:gradle:4.0.0-beta05")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4.15.2")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.2.2")
        //        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4-M1")
    }
}

repositories {
    jcenter()
    maven("https://plugins.gradle.org/m2/")
    mavenCentral()
    google()
    gradlePluginPortal()
}

fun findProperty(propertyName: String): String? =
    project.findProperty(propertyName) as String? ?: System.getenv(propertyName)

val Project.type: String?
    get() = when {
        name == rootProject.name -> "root"
        name.endsWith("app", ignoreCase = true) || name == "proguard-tests" -> "application"
        name.startsWith("ios", ignoreCase = true) -> "iosApp"
        name != "buildSrc" -> "library"
        else -> null
    }
val Project.application: Boolean get() = type == "application"
val Project.library: Boolean get() = type == "library"
val Project.iosApp: Boolean get() = type == "iosApp"

//apply(plugin = "java")

subprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    when {
        application -> {
            apply(plugin = "com.android.application")
        }
        library -> { apply(plugin = "com.android.library") }
        else -> { return@subprojects }
    }
//    apply(plugin = "org.jetbrains.kotlin.android.extensions")
//    apply(plugin = "androidx.navigation.safeargs.kotlin")
}
apply(plugin = "java")
//tasks {
//    named<Test>("test") {
//        testLogging.showExceptions = true
//        useJUnitPlatform()
//    }
//}
allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        mavenLocal()
    }
    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }
    }
}