import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import java.util.*
import java.io.*

class MyModulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
//        project.plugins.apply("kotlin-android")
        project.plugins.apply("kotlin-android-extensions")
        // Configure common android build parameters.
        val androidExtension = project.extensions.getByName("android")
        if (androidExtension is BaseExtension) {
            androidExtension.apply {
                compileSdkVersion(29)
                defaultConfig {
                    if (this@defaultConfig is com.android.build.gradle.AppExtension) {
                        applicationId = "net.csgstore.${project.rootProject.name.toLowerCase(Locale.ROOT)}"
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
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }
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
            }
        }
    }
}
@SuppressWarnings("UnnecessaryQualifiedReference")
fun configureOutputFileName(variant: com.android.build.gradle.api.BaseVariant, project: Project, baseOnly: Boolean = true) {
    variant.outputs.all {
        val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
        val buildType = variant.buildType.name
        var tmpOutputFileName = outputImpl.outputFileName
        if (variant is com.android.build.gradle.api.ApplicationVariant) {
            val fileName = if (baseOnly) "${project.name}.apk" else "${project.name}-${variant.versionName}_${buildType}.apk"
            val defaultOutputDir = variant.packageApplicationProvider!!.get().outputDirectory
            val tmpOutputFile = File(defaultOutputDir.asFile.get().absolutePath, fileName)
            project.allprojects {
                extra.set("apkFile", tmpOutputFile)
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

