@file:Suppress("RemoveRedundantBackticks")

package net.csgstore

/**
 * Retrieves the [net.csgstore.ssh.getBuildOutputs][org.gradle.api.NamedDomainObjectContainer<com.android.build.gradle.api.BaseVariantOutput>] extension.
 */
@Suppress("UNCHECKED_CAST") val org.gradle.api.Project.`buildOutputs`: org.gradle.api.NamedDomainObjectContainer<com.android.build.gradle.api.BaseVariantOutput> get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("buildOutputs") as org.gradle.api.NamedDomainObjectContainer<com.android.build.gradle.api.BaseVariantOutput>

/**
 * Configures the [net.csgstore.ssh.getBuildOutputs][org.gradle.api.NamedDomainObjectContainer<com.android.build.gradle.api.BaseVariantOutput>] extension.
 */
fun org.gradle.api.Project.`buildOutputs`(configure: org.gradle.api.NamedDomainObjectContainer<com.android.build.gradle.api.BaseVariantOutput>.() -> Unit): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("buildOutputs", configure)