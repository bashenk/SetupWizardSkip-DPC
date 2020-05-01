plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
    `java-library`
    kotlin("jvm") version embeddedKotlinVersion
}

/*gradlePlugin {
    plugins {
        register("ssh") {
            id = "net.csgstore.ssh"
            version = "1.0.0"
            implementationClass = "net.csgstore.Ssh"
        }
    }
}*/

buildscript {
    repositories {
        jcenter()
        maven("https://plugins.gradle.org/m2/")
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$embeddedKotlinVersion")
        classpath("org.hidetake:gradle-ssh-plugin:2.10.1")
    }
}

repositories {
    jcenter()
    maven("https://plugins.gradle.org/m2/")
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    compileOnly(gradleApi())
    implementation(localGroovy())
    testCompileOnly(gradleTestKit())
    implementation(gradleKotlinDsl())
    implementation("com.android.tools.build:gradle:4.0.0-beta05")

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("gradle-plugin"))
    implementation(kotlin("android-extensions"))

    testImplementation("junit:junit:4.12")
    testImplementation("org.assertj:assertj-core:3.6.2")

//    implementation("org.jacoco:org.jacoco.core:0.8.4")
//    implementation(group = "commons-io", name = "commons-io", version= "2.0.1")

    implementation("org.hidetake:gradle-ssh-plugin:2.10.1")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
