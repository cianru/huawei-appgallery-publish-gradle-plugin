package ru.cian.huawei.publish

import com.android.build.api.artifact.ArtifactType
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.InstallableVariantImpl
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import ru.cian.huawei.publish.models.Credential
import ru.cian.huawei.publish.models.request.FileInfoRequest
import ru.cian.huawei.publish.models.response.FileServerOriResultResponse
import ru.cian.huawei.publish.service.HuaweiService
import ru.cian.huawei.publish.service.HuaweiServiceImpl
import ru.cian.huawei.publish.utils.Logger
import ru.cian.huawei.publish.utils.nullIfBlank
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import javax.inject.Inject

open class HuaweiPublishTask
@Inject constructor(
    private val variant: BaseVariant
) : DefaultTask() {

    init {
        group = "Huawei App Gallery Publishing"
        description = "Upload and publish application build file to Huawei AppGallery Store for ${variant.baseName} buildType"
    }

    @get:Internal
    @set:Option(option = "no-publish", description = "To disable publishing the build file on all users after uploading")
    var noPublish: Boolean? = null
        set(value) {
            if (value != null) {
                field = !value
            }
        }

    @get:Internal
    @set:Option(option = "publish", description = "To enable publishing the build file on all users after uploading")
    var publish: Boolean? = null

    @get:Internal
    @set:Option(option = "credentialsPath", description = "File path with AppGallery credentials params ('client_id' and 'client_key')")
    var credentialsPath: String? = null

    @get:Internal
    @set:Option(option = "buildFormat", description = "'apk' or 'aab' for corresponding build format")
    var buildFormat: BuildFormat? = null

    @get:Internal
    @set:Option(option = "buildFile", description = "Path to build file. 'null' means use standard path for 'apk' and 'aab' files.")
    var buildFile: String? = null

    @TaskAction
    fun action() {

        val huaweiService: HuaweiService = HuaweiServiceImpl()
        val huaweiPublishExtension = project.extensions.findByName(HuaweiPublishExtension.NAME) as? HuaweiPublishExtension
            ?: throw IllegalArgumentException("Plugin extension '${HuaweiPublishExtension.NAME}' is not available at app/build.gradle")

        val buildTypeName = variant.name
        val extension = huaweiPublishExtension.instances.find { it.name.toLowerCase() == buildTypeName.toLowerCase() }
            ?: throw IllegalArgumentException("Plugin extension '${HuaweiPublishExtension.NAME}' instance with name '$buildTypeName' is not available")

        val publish = this.noPublish ?: this.publish ?: extension.publish ?: true
        val credentialsFilePath = this.credentialsPath ?: extension.credentialsPath
        val buildFormat = this.buildFormat ?: extension.buildFormat
        val buildFile: String? = this.buildFile ?: extension.buildFile

        val credentialsFile = File(credentialsFilePath)
        if (!credentialsFile.exists()) {
            throw FileNotFoundException("$huaweiPublishExtension (File (${credentialsFile.absolutePath}) with 'client_id' and 'client_key' for access to Huawei Publish API is not found)")
        }

        val apkBuildFiles = when {
            buildFile != null -> File(buildFile)
            buildFormat == BuildFormat.APK -> getFinalApkArtifactCompat(variant)
            buildFormat == BuildFormat.AAB -> getFinalBundleArtifactCompat(variant).singleOrNull()
            else -> throw FileNotFoundException("Could not detect build file path")
        }

        if (apkBuildFiles == null || !apkBuildFiles.exists()) {
            throw FileNotFoundException("$apkBuildFiles (No such file or directory). Please run `assemble*` or `bundle*` task to build the application file before current task.")
        }

        if (buildFormat.fileExtension != apkBuildFiles.extension) {
            throw IllegalArgumentException("Build file ${apkBuildFiles.absolutePath} has wrong file extension that doesn't match with announced buildFormat($buildFormat) plugin extension param.")
        }

        val buildFileName = apkBuildFiles.name
        Logger.i("Found build file: `${buildFileName}`")

        Logger.i("Get Credentials")
        val credentials = getCredentials(credentialsFile)
        val clientId = credentials.clientId.nullIfBlank()
            ?: throw IllegalArgumentException("(Huawei credential `clientId` param is null or empty). Please check your credentials file content.")
        val clientSecret = credentials.clientKey.nullIfBlank()
            ?: throw IllegalArgumentException("(Huawei credential `clientSecret` param is null or empty). Please check your credentials file content.")

        Logger.i("Get Access Token")
        val token = huaweiService.getToken(
            clientId = clientId,
            clientSecret = clientSecret
        )

        Logger.i("Get App ID")
        val appInfo = huaweiService.getAppID(
            clientId = clientId,
            token = token,
            packageName = variant.applicationId
        )

        Logger.i("Get Upload Url")
        val uploadUrl = huaweiService.getUploadingBuildUrl(
            clientId = clientId,
            token = token,
            appId = appInfo.value,
            suffix = buildFormat.fileExtension
        )

        Logger.i("Upload build file '${apkBuildFiles.path}'")
        val fileInfoListResult = huaweiService.uploadBuildFile(
            uploadUrl = uploadUrl.uploadUrl,
            authCode = uploadUrl.authCode,
            buildFile = apkBuildFiles
        )

        Logger.i("Update App File Info")
        val fileInfoRequestList = mapFileInfo(fileInfoListResult, buildFileName)
        val appId = appInfo.value
        huaweiService.updateAppFileInformation(
            clientId = clientId,
            token = token,
            appId = appId,
            fileInfoRequestList = fileInfoRequestList
        )

        if (publish) {
            Logger.i("Submit Release")
            huaweiService.submitPublication(
                clientId = clientId,
                token = token,
                appId = appId
            )
            Logger.i("Upload build file with submit on user - Successfully Done!")
        } else {
            Logger.i("Upload build file without submit on user - Successfully Done!")
        }
    }

    private fun mapFileInfo(
        fileInfoListResult: FileServerOriResultResponse,
        buildFileName: String
    ): MutableList<FileInfoRequest> {
        val fileInfoList = fileInfoListResult.result.uploadFileRsp?.fileInfoList
        val fileInfoRequestList = mutableListOf<FileInfoRequest>()
        fileInfoList?.forEach {
            fileInfoRequestList.add(
                FileInfoRequest(
                    fileName = buildFileName,
                    fileDestUrl = it.fileDestUlr,
                    size = it.size
                )
            )
        }
        return fileInfoRequestList
    }

    private fun getCredentials(credentialsFile: File): Credential {
        val reader = JsonReader(FileReader(credentialsFile.absolutePath))
        val type = object : TypeToken<Credential>() {}.type
        return Gson().fromJson(reader, type)
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

    companion object {
        const val NAME = "publishHuaweiAppGallery"
    }
}