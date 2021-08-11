package ru.cian.huawei.publish.utils

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.variant.ApplicationVariant
import ru.cian.huawei.publish.BuildFormat
import java.io.File

internal class BuildFileProvider(private val variant: ApplicationVariant) {

    fun getBuildFile(buildFormat: BuildFormat): File? {
        return when(buildFormat) {
            BuildFormat.APK -> getFinalApkArtifactCompat(variant).singleOrNull()
            BuildFormat.AAB -> getFinalBundleArtifactCompat(variant).singleOrNull()
        }
    }

    private fun getFinalApkArtifactCompat(variant: ApplicationVariant): List<File> {
        return variant.artifacts.get(ArtifactType.APK).map {
            variant.artifacts.getBuiltArtifactsLoader().load(it)
                ?.elements?.map { element -> File(element.outputFile) } ?: emptyList()
        }.getOrElse( emptyList())
    }

    private fun getFinalBundleArtifactCompat(variant: ApplicationVariant): List<File> {
        return variant.artifacts.get(ArtifactType.BUNDLE).map {
            listOf(File(it.asFile.absolutePath))
        }.getOrElse( emptyList())
    }
}