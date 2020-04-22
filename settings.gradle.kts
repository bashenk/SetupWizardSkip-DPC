pluginManagement {
    resolutionStrategy.eachPlugin {
//        println("${requested}\n\tmodule:\t\t${requested.module}\n\tversion:\t${requested.version}\n\tid:\t\t\t${requested.id.id}\n\tname:\t\t${requested.id.name}\n\tnamespace:\t${requested.id.namespace}")
        when (requested.id.id) {
            "nebula.lint" -> useModule("com.netflix.nebula:gradle-lint-plugin:26.2.1")
            "com.google.gms.google-services" -> useModule("com.google.gms:google-services:16.0.1")
            "io.fabric" -> useModule("io.fabric.tools:gradle:1.4.8")
            else -> when {
                requested.id.id.startsWith("com.android", true) -> {
                    useVersion("4.0.0-beta04")
                }
                requested.id.namespace.toString().startsWith("org.jetbrains.kotlin",true) -> {
                    println("${requested.id.id}: ${requested.version}")
                    useVersion("1.3.72")
                }
            }
        }
    }
    repositories {
        maven("https://plugins.gradle.org/m2/")
        mavenCentral()
        google()
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://maven.fabric.io/public")
        maven("https://maven.google.com")
        maven("https://jitpack.io")
        mavenLocal()
    }
}
fun PluginResolveDetails.getVersion(fallback: String) =
    if (!requested.version.isNullOrBlank()) {
        requested.version as String
    } else {
        fallback
    }

/*plugins {
    id("com.gradle.enterprise") version ("3.2.1")
}*/

/*gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"

        publishAlways()
    }
}*/

// Configure this per project
rootProject.name = "SetupSkip"

rootProject.buildFileName = "build.gradle.kts"
include(":app")
//enableFeaturePreview("GRADLE_METADATA")
