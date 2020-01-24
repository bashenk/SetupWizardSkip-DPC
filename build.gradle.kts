buildscript {
    repositories {
        google()
        jcenter()
        maven("https://maven.google.com")
        maven ("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://maven.pkg.github.com/")
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
    dependencies {
        classpath(group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin")
        classpath("com.android.tools.build:gradle:3.5.3")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4.9.7")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.2.0")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.70-eap-42")
   }
}

plugins {
    base
    id("com.gradle.build-scan") version "2.3"
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    kotlin("android") apply false
//    kotlin("android.extensions") apply false
    kotlin("kapt") apply false
    id("org.hidetake.ssh") version "2.10.1"
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

subprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    when {
        application -> { 
            apply(plugin = "com.android.application")
            apply(plugin = "org.hidetake.ssh") 
        }
        library -> { apply(plugin = "com.android.library") }
        else -> { return@subprojects }
    }
//    apply(plugin = "org.jetbrains.kotlin.android.extensions")
//    apply(plugin = "androidx.navigation.safeargs.kotlin")
}
apply(plugin = "java")
tasks {
    named<Test>("test") {
        testLogging.showExceptions = true
        useJUnitPlatform()
    }
}
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
