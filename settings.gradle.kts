pluginManagement {
    resolutionStrategy.eachPlugin {
//        println("${requested}\n\tmodule:\t\t${requested.module}\n\tversion:\t${requested.version}\n\tid:\t\t\t${requested.id.id}\n\tname:\t\t${requested.id.name}\n\tnamespace:\t${requested.id.namespace}")
        when (requested.id.id) {
            "nebula.lint" -> useModule("com.netflix.nebula:gradle-lint-plugin:${getVersion("26.2.1")}")
            "com.google.gms.google-services" -> useModule("com.google.gms:google-services:${getVersion("16.0.1")}")
            "io.fabric" -> useModule("io.fabric.tools:gradle:${getVersion("1.4.8")}")
            else -> when {
                requested.id.id.startsWith("com.android", true) -> {
                    useVersion(getVersion("3.5.5"))
                }
                requested.id.namespace.toString().startsWith("org.jetbrains.kotlin",true) -> {
                    useVersion(getVersion("1.3.50"))
                }
            }
        }
    }
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://maven.fabric.io/public")
        maven("https://maven.google.com")
        maven("https://jitpack.io")
        maven("https://plugins.gradle.org/m2/")
        mavenLocal()
        gradlePluginPortal()
    }
}
// Configure this per project
rootProject.name = "SetupSkip"

rootProject.buildFileName = "build.gradle.kts"
include(":app")
enableFeaturePreview("GRADLE_METADATA")

fun PluginResolveDetails.getVersion(fallback: String) =
        if (!requested.version.isNullOrBlank()) {
//            println("\tUsing ${requested.version}")
            requested.version as String
        } else {
//            println("\tUsing $fallback")
            fallback
        }