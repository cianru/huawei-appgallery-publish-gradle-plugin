package ru.cian.huawei.publish.utils

import com.android.build.api.artifact.ArtifactType
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.InstallableVariantImpl
import com.android.build.gradle.internal.scope.InternalArtifactType
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import ru.cian.huawei.publish.models.BuildFormat
import java.io.File

internal class BuildFileProvider(private val variant: BaseVariant) {

    fun getBuildFile(buildFormat: BuildFormat): File? {
        return when(buildFormat) {
            BuildFormat.APK -> getFinalApkArtifactCompat(variant)
            BuildFormat.AAB -> getFinalBundleArtifactCompat(variant).singleOrNull()
        }
    }

    private fun getFinalApkArtifactCompat(variant: BaseVariant): File {
        return variant.outputs.first().outputFile
    }

    @Suppress("UNCHECKED_CAST") // We know its type
    private fun getFinalBundleArtifactCompat(variant: BaseVariant): Set<File> {
        val installable = variant as InstallableVariantImpl
        return try {
            installable.getFinalArtifact(
                InternalArtifactType.BUNDLE as ArtifactType<FileSystemLocation>
            ).get().files
        } catch (e: NoClassDefFoundError) {
            val enumMethod =
                InternalArtifactType::class.java.getMethod("valueOf", String::class.java)
            val artifactType = enumMethod.invoke(null, "BUNDLE") as ArtifactType<RegularFile>
            val artifact = installable.javaClass
                .getMethod("getFinalArtifact", ArtifactType::class.java)
                .invoke(installable, artifactType)
            artifact.javaClass.getMethod("getFiles").apply {
                isAccessible = true
            }.invoke(artifact) as Set<File>
        }
    }
}