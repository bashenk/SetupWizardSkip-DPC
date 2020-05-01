package net.csgstore

import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import org.gradle.api.Project
import org.gradle.kotlin.dsl.contains
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.project
import java.io.File

const val DEBUG = true

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
    variant: com.android.build.gradle.api.BaseVariant, project: Project, formatString: String = "%1\$s-%2\$s_%3\$s"
) {
    variant.outputs.all {
        val fileName =
            formatString.format(project.name, variant.generateBuildConfigProvider.get().versionName.get(), variant.buildType.name)
        val tmpOutputFile: File = when (variant) {
            is com.android.build.gradle.api.ApplicationVariant -> File(
                variant.packageApplicationProvider!!.get().outputDirectory.asFile.get().absolutePath, "$fileName.apk")
            is com.android.build.gradle.api.LibraryVariant -> File(
                variant.packageLibraryProvider!!.get().destinationDirectory.asFile.get().absolutePath, "$fileName.aar")
            else -> outputFile
        }
        (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName = tmpOutputFile.name
        println("Output file set to \"${tmpOutputFile.canonicalPath}\"")
    }
}

fun String.encodeToUrl(padding: Boolean = true): String? {
    val encoder = if (padding) Base64.getUrlEncoder() else Base64.getUrlEncoder().withoutPadding()
    return encoder.encodeToString(this.toByteArray(UTF_8))
}

fun ByteArray.encodeBase64URLSafe(): String = org.apache.commons.codec.binary.Base64.encodeBase64URLSafe(this).toString(
    UTF_8)

fun String.encodeBase64URLSafe(): String =
    org.apache.commons.codec.binary.Base64.encodeBase64URLSafe(this.toByteArray(UTF_8)).toString(
        UTF_8)

fun Project.runCommand(string: String): String {
    val byteOut = org.apache.commons.io.output.ByteArrayOutputStream()
    project.exec {
        commandLine = string.split(" ")
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}

fun Project.getChecksums(androidSdkPath: String = "C:/Android/sdk/") {
    val buildToolsVersion = "30.0.0-rc2"
    val apkFile = if (project.extra.has("outputFile")) project.extra["outputFile"] as File else null
    if (apkFile == null) {
        println("Could not find outputFile in ${::getChecksums.name}")
        return
    }
    val output = runCommand("$androidSdkPath/build-tools/$buildToolsVersion/apksigner.bat verify -print-certs ${apkFile.absolutePath}")
    val arr = output.split("\r\n")
    arr.filter { str: String -> str.contains("SHA-256 digest:") }.forEach {
        val start = it.indexOf("#")
        println("${it.trim().subSequence(it.indexOf(" ", start) + 1, it.indexOf("digest") - 1)}")
        val sha = it.trim().takeLast(64)
        println("SHA-256 is:\t$sha")
        println("Url-safe Base64 (padded):\t${sha.encodeToUrl(true)}")
        println("Url-safe Base64:\t\t\t${sha.encodeToUrl(false)}")
        println("Url-safe Base64:\t\t\t${sha.encodeBase64URLSafe()}")
        println()
    }
}