import com.android.build.gradle.api.ApplicationVariant

android {
    signingConfigs {
        register("release") {
            keyAlias = rootProject.property("ANDROID_KEY_ALIAS").toString()
            keyPassword = rootProject.property("ANDROID_KEY_PASSWORD").toString()
            storeFile = file(rootProject.property("ANDROID_STORE_FILE").toString())
            storePassword = rootProject.property("ANDROID_STORE_PASSWORD").toString()
        }
    }
    compileSdkVersion(29)
    defaultConfig {
        if (this@defaultConfig is com.android.build.gradle.AppExtension) {
            applicationId = "net.csgstore.${rootProject.name.toLowerCase()}"
            applicationVariants.all { configureOutputFileName(this, project.rootProject) }
        } else if (this@defaultConfig is com.android.build.gradle.LibraryExtension) {
            libraryVariants.all { configureOutputFileName(this, project.rootProject) }
        }
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 2
        versionName = "1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isShrinkResources = false
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        create("qa") {
            buildConfigField("Boolean", "PLACEHOLDER", true.toString())
            isDebuggable = true
            isShrinkResources = true
            isMinifyEnabled = true
            //            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            isDebuggable = false
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            //            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        testOptions {
            unitTests.isIncludeAndroidResources = true
            unitTests.isReturnDefaultValues = true
        }
    }
    testBuildType = "debug"
    lintOptions {
        disable("ObsoleteLintCustomCheck", // ButterKnife will fix this in v9.0
            "IconExpectedSize", "InvalidPackage", // Firestore uses GRPC which makes lint mad
            "NewerVersionAvailable", "GradleDependency", // For reproducible builds
            "SelectableText", "SyntheticAccessor" // We almost never care about this
        )
        isCheckAllWarnings = true
        isWarningsAsErrors = true
        isAbortOnError = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        exclude("META-INF/*.kotlin_module")
        //        exclude("META-INF/kotlin-stdlib*")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("androidx.legacy:legacy-preference-v14:1.0.0")
    implementation(group = "commons-io", name = "commons-io", version= "2.0.1")
    //    implementation(kotlin("stdlib"))
    //    implementation("androidx.appcompat:appcompat:1.1.0-rc01")
    //    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    /*    // Core library
        implementation("androidx.test:core:1.0.0")

        // AndroidJUnitRunner and JUnit Rules
        implementation("androidx.test:runner:1.1.0")
        implementation("androidx.test:rules:1.1.0")
        // Assertions
        implementation("androidx.test.ext:junit:1.0.0")
        implementation("androidx.test.ext:truth:1.0.0")
        implementation("com.google.truth:truth:0.42")

        // Espresso dependencies
        implementation("androidx.test.espresso:espresso-core:3.1.0")
        implementation("androidx.test.espresso:espresso-contrib:3.1.0")
        implementation("androidx.test.espresso:espresso-intents:3.1.0")
        implementation("androidx.test.espresso:espresso-accessibility:3.1.0")
        implementation("androidx.test.espresso:espresso-web:3.1.0")
        implementation("androidx.test.espresso.idling:idling-concurrent:3.1.0")

        // Optional -- UI testing with UI Automator
        implementation("androidx.test.uiautomator:uiautomator:2.2.0")

        // The following Espresso dependency can be either "implementation"
        // or "androidTestImplementation", depending on whether you want the
        // dependency to appear on your APK's compile classpath or the test APK
        // classpath.
        implementation("androidx.test.espresso:espresso-idling-resource:3.1.0")
        */


    /*    // Core library
        androidTestImplementation("androidx.test:core:1.0.0")

        // AndroidJUnitRunner and JUnit Rules
        androidTestImplementation("androidx.test:runner:1.1.0")
        androidTestImplementation("androidx.test:rules:1.1.0")
        // Assertions
        androidTestImplementation("androidx.test.ext:junit:1.0.0")
        androidTestImplementation("androidx.test.ext:truth:1.0.0")
        androidTestImplementation("com.google.truth:truth:0.42")

        // Espresso dependencies
        androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0")
        androidTestImplementation("androidx.test.espresso:espresso-contrib:3.1.0")
        androidTestImplementation("androidx.test.espresso:espresso-intents:3.1.0")
        androidTestImplementation("androidx.test.espresso:espresso-accessibility:3.1.0")
        androidTestImplementation("androidx.test.espresso:espresso-web:3.1.0")
        androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:3.1.0")

        // The following Espresso dependency can be either "implementation"
        // or "androidTestImplementation", depending on whether you want the
        // dependency to appear on your APK's compile classpath or the test APK
        // classpath.
        androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.1.0")*/

    // Optional -- UI testing with UI Automator
    //    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    //    implementation(kotlin("reflect"))

}

kotlin {
    android("android")

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                //                implementation(kotlin("stdlib-jdk8"))
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
    }
//    experimental.coroutines = org.jetbrains.kotlin.gradle.dsl.Coroutines.DEFAULT
}

@SuppressWarnings("UnnecessaryQualifiedReference")
fun configureOutputFileName(variant: com.android.build.gradle.api.BaseVariant, project: Project, baseOnly: Boolean = true) {
    variant.outputs.all {
        val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
        val buildType = variant.buildType.name
        var tmpOutputFileName = outputImpl.outputFileName
        if (variant is ApplicationVariant) {
            val fileName = if (baseOnly) "${project.name}.apk" else "${project.name}-${variant.versionName}_${buildType}.apk"
            val defaultOutputDir = variant.packageApplicationProvider!!.get().outputDirectory
            val tmpOutputFile = File(defaultOutputDir.asFile.get().absolutePath, fileName)
            allprojects {
                ext {
                    set("apkFile", tmpOutputFile)
                }
            }
            tmpOutputFileName = tmpOutputFile.name
        } else if (variant is com.android.build.gradle.api.LibraryVariant) {
            val fileName = if (baseOnly) "${project.name}.apk" else "${project.name}_${buildType}.apk"
            val defaultOutputDir = variant.packageLibraryProvider!!.get().destinationDirectory.asFile.get()
            tmpOutputFileName = File(defaultOutputDir.absolutePath, fileName).name
        }
        outputImpl.outputFileName = tmpOutputFileName
        //        println(tmpOutputFileName)
    }
}

fun findProperty(propertyName: String): String? = project.findProperty(propertyName) as String? ?: System.getenv(propertyName)

@Suppress("unused")
fun DependencyHandler.lamba(module: String, version: String? = null): Any =
    "com.github.lamba92:$module${version?.let { ":$version" } ?: ""}"

apply (plugin = "org.hidetake.ssh")
//apply { from("ssh.gradle.kts") }
