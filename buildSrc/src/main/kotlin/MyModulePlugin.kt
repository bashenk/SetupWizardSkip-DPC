import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.*

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
                    if (this is com.android.build.gradle.AppExtension) {
                        applicationId =
                            "net.csgstore.${project.rootProject.name.toLowerCase(Locale.ROOT)}"
                        applicationVariants.all {
                            configureOutputFileName(this, project.rootProject)
                        }
                    } else if (this is com.android.build.gradle.LibraryExtension) {
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
                    disable(
                        "ObsoleteLintCustomCheck", // ButterKnife will fix this in v9.0
                        "IconExpectedSize",
                        "InvalidPackage", // Firestore uses GRPC which makes lint mad
                        "NewerVersionAvailable",
                        "GradleDependency", // For reproducible builds
                        "SelectableText",
                        "SyntheticAccessor" // We almost never care about this
                    )
                    isCheckAllWarnings = true
                    isWarningsAsErrors = true
                    isAbortOnError = true
                }
            }
        }
    }
}

/**
 * Configures the output file names for all outputs of the provided variant. That is, for the
 * provided application or library.
 *
 * @param variant Passed in with {android.defaultConfig.applicationVariants.all.this}
 * @param project The project from which to grab the filename. Tip: Use rootProject
 * @param formatString Format string for the filename, which will be called with three arguments:
 * (1) Project Name, (2) Version Name, (3) Build Type. ".apk" or ".aar" is automatically
 * appended. If not provided, defaults to "%0$s-%1$s_%2$s"
 */
@SuppressWarnings("UnnecessaryQualifiedReference")
fun configureOutputFileName(
    variant: com.android.build.gradle.api.BaseVariant,
    project: Project,
    formatString: String = "%1\$s-%2\$s_%3\$s"
) {
    variant.outputs.all {
        val fileName = formatString.format(
            project.name,
            variant.generateBuildConfigProvider.get().versionName.get(),
            variant.buildType.name
        )
        val tmpOutputFile: File = when (variant) {
            is com.android.build.gradle.api.ApplicationVariant ->
                File(
                    variant.packageApplicationProvider!!.get().outputDirectory.asFile
                        .get().absolutePath, "$fileName.apk"
                )
            is com.android.build.gradle.api.LibraryVariant     ->
                File(
                    variant.packageLibraryProvider!!.get().destinationDirectory.asFile
                        .get().absolutePath, "$fileName.aar"
                )
            else                                               -> outputFile
        }
        (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl)
            .outputFileName = tmpOutputFile.name
        println("Output file set to \"${tmpOutputFile.canonicalPath}\"")
    }
}

