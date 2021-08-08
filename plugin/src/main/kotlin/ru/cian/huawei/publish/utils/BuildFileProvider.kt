package ru.cian.huawei.publish.utils

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.Variant
import ru.cian.huawei.publish.BuildFormat
import java.io.File

internal class BuildFileProvider(private val variant: Variant) {

    fun getBuildFile(buildFormat: BuildFormat): File? {
        val artifactType = when(buildFormat) {
            BuildFormat.APK -> SingleArtifact.APK
            BuildFormat.AAB -> SingleArtifact.BUNDLE
        }
        return variant.artifacts.get(artifactType).get().asFile
    }
}