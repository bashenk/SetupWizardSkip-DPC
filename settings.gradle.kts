
plugins {
    id("com.gradle.enterprise") version ("3.2.1")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"

        publishAlways()
    }
}

// Configure this per project
rootProject.name = "HotspotKiosk"

rootProject.buildFileName = "build.gradle.kts"

include(":app")
