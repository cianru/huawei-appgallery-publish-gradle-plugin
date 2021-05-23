package ru.cian.huawei.publish.utils

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.variant.ApplicationVariantProperties
import ru.cian.huawei.publish.BuildFormat
import java.io.File

internal class BuildFileProvider(private val variant: ApplicationVariantProperties) {

    fun getBuildFile(buildFormat: BuildFormat): File? {
        return when(buildFormat) {
            BuildFormat.APK -> getFinalApkArtifactCompat(variant)
            BuildFormat.AAB -> getFinalBundleArtifactCompat(variant)
        }
    }

    private fun getFinalApkArtifactCompat(variant: ApplicationVariantProperties): File? {
        val artifacts = variant.artifacts
        val artifactType = artifacts.get(ArtifactType.APK).get()
        val sneakyNull = artifacts.getBuiltArtifactsLoader()
            .load(artifactType)?.elements?.map { it.outputFile } ?: return null
        return File(sneakyNull.first())
    }

    /**
     * That's hack&trick due to https://issuetracker.google.com/issues/109918868
     */
    private fun getFinalBundleArtifactCompat(variant: ApplicationVariantProperties): File? {
        val artifactType = variant.artifacts.get(ArtifactType.BUNDLE)
        val filePath = artifactType.get().asFile.absolutePath ?: return null
        return File(filePath)
    }
}