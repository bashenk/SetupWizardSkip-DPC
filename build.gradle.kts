buildscript {
    repositories {
        google()
        jcenter()
        maven("https://maven.google.com")
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
    dependencies {
        classpath(group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin")
        classpath("com.android.tools.build:gradle:3.5.0")
        classpath("com.google.gms:google-services:4.3.2")
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4.9.7")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.2.0-alpha02")
    }
}

plugins {
    base
    id("com.gradle.build-scan") version "2.3"
    kotlin("jvm") apply false
    id("org.jetbrains.kotlin.multiplatform") apply false
    kotlin("android") apply false
    kotlin("android.extensions") apply false
    kotlin("kapt") apply false
}

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
        application -> { apply(plugin = "com.android.application") }
        library -> { apply(plugin = "com.android.library") }
        else -> { return@subprojects }
    }
    apply(plugin = "org.jetbrains.kotlin.android.extensions")
    apply(plugin = "androidx.navigation.safeargs.kotlin")
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
