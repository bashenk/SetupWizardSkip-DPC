plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    maven("https://plugins.gradle.org/m2/")
    mavenCentral()
    google()
    jcenter()
    gradlePluginPortal()
}

dependencies {
    compileOnly(gradleApi())
    implementation(kotlin("stdlib"))
    testCompileOnly(gradleTestKit())
    testImplementation("junit:junit:4.12")
    testImplementation("org.assertj:assertj-core:3.6.2")

    implementation("com.android.tools.build:gradle:4.0.0-beta04")
    implementation(kotlin("gradle-plugin", "1.3.72"))

    implementation(kotlin("android-extensions"))
    implementation("org.jacoco:org.jacoco.core:0.8.4")
    implementation(group = "commons-io", name = "commons-io", version= "2.0.1")

    implementation("org.hidetake:gradle-ssh-plugin:2.10.1")
}