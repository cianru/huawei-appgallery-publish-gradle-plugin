package ru.cian.huawei.publish.utils

import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import ru.cian.huawei.publish.BuildFormat
import java.io.File

internal class BuildFileProvider(
    private val apkDirectory: Directory?,
    private val builtArtifactsLoader: BuiltArtifactsLoader?,
    private val bundleFile: RegularFile?,
    private val logger: Logger,
) {

    fun getBuildFile(buildFormat: BuildFormat): File? {
        return when (buildFormat) {
            BuildFormat.APK -> getFinalApkArtifactCompat().singleOrNull()
            BuildFormat.AAB -> bundleFile?.asFile
        }
    }

    // TODO(a.mirko): Remove after https://github.com/gradle/gradle/issues/16777
    // TODO(a.mirko): Remove after https://github.com/gradle/gradle/issues/16775
    private fun getFinalApkArtifactCompat(): List<File> {
        val directory = apkDirectory ?: return emptyList()
        logger.v("Build File Directory: $directory")
        val loader = builtArtifactsLoader
        val loaded = loader?.load(directory)?.elements?.map { element -> File(element.outputFile) }
        if (!loaded.isNullOrEmpty()) {
            return loaded
        }
        return directory.asFileTree.matching { include("*.apk") }.map { File(it.absolutePath) }
    }
}
