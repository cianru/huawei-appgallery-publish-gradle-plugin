package ru.cian.huawei.publish.utils

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import org.gradle.api.Project
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ru.cian.huawei.publish.BuildFormat
import ru.cian.huawei.publish.Credentials
import ru.cian.huawei.publish.DeployType
import ru.cian.huawei.publish.HuaweiPublishCliParam
import ru.cian.huawei.publish.HuaweiPublishConfig
import ru.cian.huawei.publish.HuaweiPublishExtensionConfig
import ru.cian.huawei.publish.ReleasePhaseConfig
import ru.cian.huawei.publish.ReleasePhaseExtension
import ru.cian.huawei.publish.models.Credential
import java.io.File
import ru.cian.huawei.publish.ReleaseNote
import ru.cian.huawei.publish.ReleaseNotesConfig
import ru.cian.huawei.publish.ReleaseNotesExtension
import ru.cian.huawei.publish.ReleaseNotesDescriptionsConfig

private const val DEFAULT_PUBLISH_SOCKET_TIMEOUT_IN_SECONDS = 60L
private const val DEFAULT_PUBLISH_TIMEOUT_MS = 10 * 60 * 1000L
private const val DEFAULT_PUBLISH_PERIOD_MS = 15 * 1000L
private const val BUILD_DIRECTORY_PATH = "./build"

private const val WRONG_ARTIFACT_FILE_PATH = "$BUILD_DIRECTORY_PATH/wrong_file.txt"

private const val ARTIFACT_APK_FILE_PATH = "$BUILD_DIRECTORY_PATH/file.apk"
private const val ARTIFACT_APK_FILE_SECOND_PATH = "$BUILD_DIRECTORY_PATH/file_second.apk"

private const val ARTIFACT_AAB_FILE_PATH = "$BUILD_DIRECTORY_PATH/file.aab"
private const val ARTIFACT_AAB_FILE_SECOND_PATH = "$BUILD_DIRECTORY_PATH/file_second.aab"

private const val CREDENTIALS_FILE_PATH = "$BUILD_DIRECTORY_PATH/credentials.json"
private const val CREDENTIALS_JSON = "{\"client_id\": \"id\", \"client_secret\": \"secret\"}"

private const val CREDENTIALS_FILE_SECOND_PATH = "$BUILD_DIRECTORY_PATH/credentials_second.json"
private const val CREDENTIALS_SECOND_JSON = "{\"client_id\": \"no_id\", \"client_secret\": \"no_secret\"}"

private const val APP_BASIC_INFO_FILE_PATH = "$BUILD_DIRECTORY_PATH/app_info.json"
private const val APP_BASIC_INFO_FILE_SECOND_PATH = "$BUILD_DIRECTORY_PATH/app_info_second.json"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ConfigProviderTest {

    private val buildFileProvider = mockk<BuildFileProvider>()
    private val project = mockk<Project>()
    private val releaseNotesFileProvider = mockk<FileWrapper>()

    private val emptyCliConfig = HuaweiPublishCliParam()

    private fun extensionConfigInstance() = run {
        HuaweiPublishExtensionConfig("any", project).apply {
            credentialsPath = CREDENTIALS_FILE_PATH
        }
    }

    @BeforeAll
    internal fun beforeAll() {
        File(ARTIFACT_APK_FILE_PATH).createNewFile()
        File(ARTIFACT_APK_FILE_SECOND_PATH).createNewFile()

        File(ARTIFACT_AAB_FILE_PATH).createNewFile()
        File(ARTIFACT_AAB_FILE_SECOND_PATH).createNewFile()

        File(WRONG_ARTIFACT_FILE_PATH).createNewFile()

        val credentialsFile = File(CREDENTIALS_FILE_PATH)
        credentialsFile.createNewFile()
        credentialsFile.writeText(CREDENTIALS_JSON)

        val credentialsSecondFile = File(CREDENTIALS_FILE_SECOND_PATH)
        credentialsSecondFile.createNewFile()
        credentialsSecondFile.writeText(CREDENTIALS_SECOND_JSON)

        val basicInfoFile = File(APP_BASIC_INFO_FILE_PATH)
        basicInfoFile.createNewFile()
        basicInfoFile.writeText("{\"publishCountry\": \"BY,MD,RU,AM,AZ,GE,KZ,KG,MN,TJ,TM,UZ\"}")

        val basicInfoSecondFile = File(APP_BASIC_INFO_FILE_SECOND_PATH)
        basicInfoSecondFile.createNewFile()
        basicInfoSecondFile.writeText("{\"publishCountry\": \"BY,MD,RU,AM,AZ,GE,KZ,KG,MN,TJ,TM,UZ\"}")
    }

    @AfterAll
    internal fun afterAll() {
        File(ARTIFACT_APK_FILE_PATH).delete()
        File(ARTIFACT_APK_FILE_SECOND_PATH).delete()

        File(ARTIFACT_AAB_FILE_PATH).delete()
        File(ARTIFACT_AAB_FILE_SECOND_PATH).delete()

        File(CREDENTIALS_FILE_PATH).delete()
        File(CREDENTIALS_FILE_SECOND_PATH).delete()

        File(WRONG_ARTIFACT_FILE_PATH).delete()

        File(APP_BASIC_INFO_FILE_PATH).delete()
        File(APP_BASIC_INFO_FILE_SECOND_PATH).delete()
    }

    @BeforeEach
    fun beforeEach() {
        every { buildFileProvider.getBuildFile(BuildFormat.APK) } returns File(ARTIFACT_APK_FILE_PATH)
        every { buildFileProvider.getBuildFile(BuildFormat.AAB) } returns File(ARTIFACT_AAB_FILE_PATH)
    }

    @Test
    fun `get error to build config for wrong artifact file`() = mockkObject(CredentialHelper) {

        val cliConfig = HuaweiPublishCliParam(
            buildFormat = BuildFormat.APK,
            buildFile = WRONG_ARTIFACT_FILE_PATH
        )
        val configProvider = ConfigProvider(
            extension = extensionConfigInstance(),
            cli = cliConfig,
            buildFileProvider = buildFileProvider,
            releaseNotesFileProvider = releaseNotesFileProvider,
        )

        assertThat { configProvider.getConfig() }.hasException(IllegalArgumentException::class)
    }

    @Test
    fun `correct config for default params`() = mockkObject(CredentialHelper) {

        val expectedConfig = HuaweiPublishConfig(
            credentials = Credentials("id", "secret"),
            deployType = DeployType.PUBLISH,
            artifactFormat = BuildFormat.APK,
            artifactFile = File(ARTIFACT_APK_FILE_PATH),
            publishSocketTimeoutInSeconds = DEFAULT_PUBLISH_SOCKET_TIMEOUT_IN_SECONDS,
            publishTimeoutMs = DEFAULT_PUBLISH_TIMEOUT_MS,
            publishPeriodMs = DEFAULT_PUBLISH_PERIOD_MS,
            releaseTime = null,
            releasePhase = null,
            releaseNotes = null,
            appBasicInfoFile = null
        )

        every {
            CredentialHelper.getCredentials(match { it.absolutePath == CREDENTIALS_FILE_PATH })
        } returns Credential(clientId = "id", clientSecret = "secret")

        tableOf("expectedValue", "actualValue")
            .row(
                expectedConfig,
                ConfigProvider(
                    extension = extensionConfigInstance(),
                    cli = emptyCliConfig,
                    buildFileProvider = buildFileProvider,
                    releaseNotesFileProvider = releaseNotesFileProvider,
                )
            )
            .row(
                expectedConfig,
                ConfigProvider(
                    extension = extensionConfigInstance(),
                    cli = HuaweiPublishCliParam(
                        credentialsPath = CREDENTIALS_FILE_PATH
                    ),
                    buildFileProvider = buildFileProvider,
                    releaseNotesFileProvider = releaseNotesFileProvider,
                )
            )
            .forAll { expectedValue, actualValue ->
                assertThat(actualValue.getConfig()).isEqualTo(expectedValue)
            }
    }

    @Test
    fun `correct config with overriding common values at cli params`() {

        val expectedConfig = HuaweiPublishConfig(
            credentials = Credentials("id123", "secret123"),
            deployType = DeployType.DRAFT,
            artifactFormat = BuildFormat.AAB,
            artifactFile = File(ARTIFACT_AAB_FILE_SECOND_PATH),
            publishSocketTimeoutInSeconds = 3003L,
            publishTimeoutMs = 1001L,
            publishPeriodMs = 2002L,
            releaseTime = "2019-10-18T21:00:00+0300",
            releasePhase = ReleasePhaseConfig(
                startTime = "2021-10-18T21:00:00+0300",
                endTime = "2025-10-18T21:00:00+0300",
                percent = 10.05
            ),
            releaseNotes = null,
            appBasicInfoFile = File(APP_BASIC_INFO_FILE_SECOND_PATH)
        )

        val inputExtensionConfig = extensionConfigInstance().apply {
            credentialsPath = CREDENTIALS_FILE_PATH
            deployType = DeployType.PUBLISH
            publishTimeoutMs = 3003
            publishPeriodMs = 4004
            buildFormat = BuildFormat.APK
            buildFile = ARTIFACT_APK_FILE_PATH
            releaseTime = "2000-10-18T21:00:00+0300"
            releasePhase = ReleasePhaseExtension().apply {
                startTime = "2001-10-18T21:00:00+0300"
                endTime = "2002-10-18T21:00:00+0300"
                percent = 99.7
            }
            appBasicInfo = APP_BASIC_INFO_FILE_PATH
        }
        val inputCliConfig = HuaweiPublishCliParam(
            deployType = DeployType.DRAFT,
            publishSocketTimeoutInSeconds = "3003",
            publishTimeoutMs = "1001",
            publishPeriodMs = "2002",
            credentialsPath = CREDENTIALS_FILE_SECOND_PATH,
            clientId = "id123",
            clientSecret = "secret123",
            buildFormat = BuildFormat.AAB,
            buildFile = ARTIFACT_AAB_FILE_SECOND_PATH,
            releaseTime = "2019-10-18T21:00:00+0300",
            releasePhaseStartTime = "2021-10-18T21:00:00+0300",
            releasePhaseEndTime = "2025-10-18T21:00:00+0300",
            releasePhasePercent = "10.05",
            appBasicInfo = APP_BASIC_INFO_FILE_SECOND_PATH
        )
        val configProvider = ConfigProvider(
            extension = inputExtensionConfig,
            cli = inputCliConfig,
            buildFileProvider = buildFileProvider,
            releaseNotesFileProvider = releaseNotesFileProvider,
        )

        val actualValue = configProvider.getConfig()

        assertThat(actualValue).isEqualTo(expectedConfig)
    }

    @Suppress("LongMethod")
    @Test
    fun `correct config with overriding of publish param`() {

        val expectedConfig = HuaweiPublishConfig(
            credentials = Credentials("id", "secret"),
            deployType = DeployType.PUBLISH,
            artifactFormat = BuildFormat.APK,
            artifactFile = File(ARTIFACT_APK_FILE_PATH),
            publishSocketTimeoutInSeconds = DEFAULT_PUBLISH_SOCKET_TIMEOUT_IN_SECONDS,
            publishTimeoutMs = DEFAULT_PUBLISH_TIMEOUT_MS,
            publishPeriodMs = DEFAULT_PUBLISH_PERIOD_MS,
            releaseTime = null,
            releasePhase = null,
            releaseNotes = null,
            appBasicInfoFile = null
        )

        tableOf("expectedValue", "actualValue")
            .row(
                expectedConfig.copy(deployType = DeployType.PUBLISH),
                ConfigProvider(
                    extension = extensionConfigInstance(),
                    cli = HuaweiPublishCliParam(),
                    buildFileProvider = buildFileProvider,
                    releaseNotesFileProvider = releaseNotesFileProvider,
                )
            )
            .row(
                expectedConfig.copy(deployType = DeployType.DRAFT),
                ConfigProvider(
                    extension = extensionConfigInstance().apply {
                        deployType = DeployType.DRAFT
                    },
                    cli = HuaweiPublishCliParam(),
                    buildFileProvider = buildFileProvider,
                    releaseNotesFileProvider = releaseNotesFileProvider,
                )
            )
            .row(
                expectedConfig.copy(deployType = DeployType.UPLOAD_ONLY),
                ConfigProvider(
                    extension = extensionConfigInstance().apply {
                        deployType = DeployType.UPLOAD_ONLY
                    },
                    cli = HuaweiPublishCliParam(),
                    buildFileProvider = buildFileProvider,
                    releaseNotesFileProvider = releaseNotesFileProvider,
                )
            )
            .row(
                expectedConfig.copy(deployType = DeployType.DRAFT),
                ConfigProvider(
                    extension = extensionConfigInstance().apply {
                        deployType = DeployType.DRAFT
                    },
                    cli = HuaweiPublishCliParam(
                        deployType = null
                    ),
                    buildFileProvider = buildFileProvider,
                    releaseNotesFileProvider = releaseNotesFileProvider,
                )
            )
            .row(
                expectedConfig.copy(deployType = DeployType.UPLOAD_ONLY),
                ConfigProvider(
                    extension = extensionConfigInstance().apply {
                        deployType = DeployType.DRAFT
                    },
                    cli = HuaweiPublishCliParam(
                        deployType = DeployType.UPLOAD_ONLY
                    ),
                    buildFileProvider = buildFileProvider,
                    releaseNotesFileProvider = releaseNotesFileProvider,
                )
            )
            .forAll { expectedValue, actualValue ->
                assertThat(actualValue.getConfig()).isEqualTo(expectedValue)
            }
    }

    @Suppress("LongMethod")
    @Test
    fun `correct config with overriding release notes`() {
        val expectedConfig = HuaweiPublishConfig(
            credentials = Credentials("id", "secret"),
            deployType = DeployType.PUBLISH,
            artifactFormat = BuildFormat.APK,
            artifactFile = File(ARTIFACT_APK_FILE_PATH),
            publishSocketTimeoutInSeconds = DEFAULT_PUBLISH_SOCKET_TIMEOUT_IN_SECONDS,
            publishTimeoutMs = DEFAULT_PUBLISH_TIMEOUT_MS,
            publishPeriodMs = DEFAULT_PUBLISH_PERIOD_MS,
            releaseTime = null,
            releasePhase = null,
            releaseNotes = null,
            appBasicInfoFile = null
        )
        val langRu = "lang_ru_RU"
        val releaseNotesRu = "Some release notes for ru_RU"
        val releaseNotesRuFilePath = "/some/file/path/lang_ru_RU.txt"
        val releaseNotesRuFile = mockk<File>()

        val langEn = "lang_en_EN"
        val releaseNotesEn = "Some release notes for en_EN"
        val releaseNotesEnFilePath = "/some/file/path/lang_en_EN.txt"
        val releaseNotesEnFile = mockk<File>()

        mockkStatic("kotlin.io.FilesKt__FileReadWriteKt")
        every { releaseNotesFileProvider.getFile(releaseNotesRuFilePath) } returns releaseNotesRuFile
        every { releaseNotesRuFile.exists() } returns true
        every { releaseNotesRuFile.readText(Charsets.UTF_8) } returns releaseNotesRu

        every { releaseNotesFileProvider.getFile(releaseNotesEnFilePath) } returns releaseNotesEnFile
        every { releaseNotesEnFile.exists() } returns true
        every { releaseNotesEnFile.readText(Charsets.UTF_8) } returns releaseNotesEn

        tableOf("expectedValue", "actualValue")
            .row(
                expectedConfig.copy(releaseNotes = ReleaseNotesConfig(
                    descriptions = listOf(
                        ReleaseNotesDescriptionsConfig(
                            lang = langRu,
                            newFeatures = releaseNotesRu
                        )
                    ),
                    removeHtmlTags = false
                )),
                ConfigProvider(
                    extension = extensionConfigInstance().apply {
                        releaseNotes = ReleaseNotesExtension(
                            descriptions = listOf(
                                ReleaseNote(
                                    lang = langRu,
                                    filePath = releaseNotesRuFilePath
                                )
                            ),
                            removeHtmlTags = false
                        )
                    },
                    cli = HuaweiPublishCliParam(),
                    buildFileProvider = buildFileProvider,
                    releaseNotesFileProvider = releaseNotesFileProvider,
                )
            )
            .row(
                expectedConfig.copy(releaseNotes = ReleaseNotesConfig(
                    descriptions = listOf(
                        ReleaseNotesDescriptionsConfig(
                            lang = langRu,
                            newFeatures = releaseNotesRu
                        )
                    ),
                    removeHtmlTags = false
                )),
                ConfigProvider(
                    extension = extensionConfigInstance(),
                    cli = HuaweiPublishCliParam(
                        releaseNotes = "$langRu:$releaseNotesRuFilePath"
                    ),
                    buildFileProvider = buildFileProvider,
                    releaseNotesFileProvider = releaseNotesFileProvider,
                )
            )
            .row(
                expectedConfig.copy(releaseNotes = ReleaseNotesConfig(
                    descriptions = listOf(
                        ReleaseNotesDescriptionsConfig(
                            lang = langEn,
                            newFeatures = releaseNotesEn
                        )
                    ),
                    removeHtmlTags = false
                )),
                ConfigProvider(
                    extension = extensionConfigInstance().apply {
                        releaseNotes = ReleaseNotesExtension(
                            descriptions = listOf(
                                ReleaseNote(
                                    lang = langRu,
                                    filePath = releaseNotesRuFilePath
                                )
                            ),
                            removeHtmlTags = false
                        )
                    },
                    cli = HuaweiPublishCliParam(
                        releaseNotes = "$langEn:$releaseNotesEnFilePath"
                    ),
                    buildFileProvider = buildFileProvider,
                    releaseNotesFileProvider = releaseNotesFileProvider,
                )
            )
            .forAll { expectedValue, actualValue ->
                assertThat(actualValue.getConfig()).isEqualTo(expectedValue)
            }
    }
}
