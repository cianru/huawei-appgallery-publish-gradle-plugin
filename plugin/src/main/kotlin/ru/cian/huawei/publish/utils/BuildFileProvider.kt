package ru.cian.huawei.publish.utils

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationVariant
import ru.cian.huawei.publish.BuildFormat
import java.io.File

internal class BuildFileProvider(private val variant: ApplicationVariant) {

    fun getBuildFile(buildFormat: BuildFormat): File? {
        return when (buildFormat) {
            BuildFormat.APK -> getFinalApkArtifactCompat(variant).singleOrNull()
            BuildFormat.AAB -> getFinalBundleArtifactCompat(variant).singleOrNull()
        }
    }

    // TODO(a.mirko): Remove after https://github.com/gradle/gradle/issues/16777
    // TODO(a.mirko): Remove after https://github.com/gradle/gradle/issues/16775
    private fun getFinalApkArtifactCompat(variant: ApplicationVariant): List<File> {
        val apkDirectory = variant.artifacts.get(SingleArtifact.APK).get()
        Logger.i("Build File Directory: $apkDirectory")
        return variant.artifacts.getBuiltArtifactsLoader().load(apkDirectory)
            ?.elements?.map { element -> File(element.outputFile) }
            ?: apkDirectory.asFileTree.matching { include("*.apk") }.map { it.absolutePath }.map { File(it) }
            ?: emptyList()
    }

    private fun getFinalBundleArtifactCompat(variant: ApplicationVariant): List<File> {
        val aabFile = variant.artifacts.get(SingleArtifact.BUNDLE).get().asFile
        return listOf(aabFile)
    }
}
