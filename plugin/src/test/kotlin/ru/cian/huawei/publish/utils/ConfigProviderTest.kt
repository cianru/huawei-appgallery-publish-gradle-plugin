package ru.cian.huawei.publish.utils

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.gradle.api.Project
import org.junit.jupiter.api.*
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

private const val DEFAULT_PUBLISH_TIMEOUT_MS = 10 * 60 * 1000L
private const val DEFAULT_PUBLISH_PERIOD_MS = 15 * 1000L
private const val BUILD_DIRECTORY_PATH = "./build"

private const val ARTIFACT_APK_FILE_PATH = "$BUILD_DIRECTORY_PATH/file.apk"
private const val ARTIFACT_APK_FILE_SECOND_PATH = "$BUILD_DIRECTORY_PATH/file_second.apk"

private const val ARTIFACT_AAB_FILE_PATH = "$BUILD_DIRECTORY_PATH/file.aab"
private const val ARTIFACT_AAB_FILE_SECOND_PATH = "$BUILD_DIRECTORY_PATH/file_second.aab"

private const val CREDENTIALS_FILE_PATH = "$BUILD_DIRECTORY_PATH/credentials.json"
private const val CREDENTIALS_JSON = "{\"client_id\": \"id\", \"client_secret\": \"secret\"}"

private const val CREDENTIALS_FILE_SECOND_PATH = "$BUILD_DIRECTORY_PATH/credentials_second.json"
private const val CREDENTIALS_SECOND_JSON = "{\"client_id\": \"no_id\", \"client_secret\": \"no_secret\"}"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ConfigProviderTest {

    private val buildFileProvider = mockk<BuildFileProvider>()
    private val project = mockk<Project>()

    private val emptyCliConfig = HuaweiPublishCliParam()

    private fun extensionConfigInstance() = run { HuaweiPublishExtensionConfig("any", project) }

    @BeforeAll
    internal fun beforeAll() {
        File(ARTIFACT_APK_FILE_PATH).createNewFile()
        File(ARTIFACT_APK_FILE_SECOND_PATH).createNewFile()

        File(ARTIFACT_AAB_FILE_PATH).createNewFile()
        File(ARTIFACT_AAB_FILE_SECOND_PATH).createNewFile()

        val credentialsFile = File(CREDENTIALS_FILE_PATH)
        credentialsFile.createNewFile()
        credentialsFile.writeText(CREDENTIALS_JSON)

        val credentialsSecondFile = File(CREDENTIALS_FILE_SECOND_PATH)
        credentialsSecondFile.createNewFile()
        credentialsSecondFile.writeText(CREDENTIALS_SECOND_JSON)
    }

    @AfterAll
    internal fun afterAll() {
        File(ARTIFACT_APK_FILE_PATH).delete()
        File(ARTIFACT_APK_FILE_SECOND_PATH).delete()

        File(ARTIFACT_AAB_FILE_PATH).delete()
        File(ARTIFACT_AAB_FILE_SECOND_PATH).delete()

        File(CREDENTIALS_FILE_PATH).delete()
        File(CREDENTIALS_FILE_SECOND_PATH).delete()
    }

    @BeforeEach
    fun beforeEach() {
        every { buildFileProvider.getBuildFile(BuildFormat.APK) } returns File(ARTIFACT_APK_FILE_PATH)
        every { buildFileProvider.getBuildFile(BuildFormat.AAB) } returns File(ARTIFACT_AAB_FILE_PATH)
    }

    @Test
    fun `correct config for default params`() = mockkObject(CredentialHelper) {

        val expected = HuaweiPublishConfig(
            credentials = Credentials("id", "secret"),
            deployType = DeployType.PUBLISH,
            artifactFormat = BuildFormat.APK,
            artifactFile = File(ARTIFACT_APK_FILE_PATH),
            publishTimeoutMs = DEFAULT_PUBLISH_TIMEOUT_MS,
            publishPeriodMs = DEFAULT_PUBLISH_PERIOD_MS,
            releaseTime = null,
            releasePhase = null
        )

        every {
            CredentialHelper.getCredentials(match { it.absolutePath == CREDENTIALS_FILE_PATH })
        } returns Credential(clientId = "id", clientSecret = "secret")

        tableOf("expectedValue", "actualValue")
            .row(
                expected,
                ConfigProvider(
                    extension = extensionConfigInstance().apply {
                        credentialsPath = CREDENTIALS_FILE_PATH
                    },
                    cli = emptyCliConfig,
                    buildFileProvider = buildFileProvider
                )
            )
            .row(
                expected,
                ConfigProvider(
                    extension = extensionConfigInstance(),
                    cli = HuaweiPublishCliParam(
                        credentialsPath = CREDENTIALS_FILE_PATH
                    ),
                    buildFileProvider = buildFileProvider
                )
            )
            .forAll { expectedValue, actualValue ->
                assertThat(actualValue.getConfig()).isEqualTo(expectedValue)
            }
    }

    @Test
    fun `correct config with overriding common values at cli params`() {

        val expectedValue = HuaweiPublishConfig(
            credentials = Credentials("id123", "secret123"),
            deployType = DeployType.DRAFT,
            artifactFormat = BuildFormat.AAB,
            artifactFile = File(ARTIFACT_AAB_FILE_SECOND_PATH),
            publishTimeoutMs = 1001L,
            publishPeriodMs = 2002L,
            releaseTime = "2019-10-18T21:00:00+0300",
            releasePhase = ReleasePhaseConfig(
                startTime = "2021-10-18T21:00:00+0300",
                endTime = "2022-10-18T21:00:00+0300",
                percent = 10.05
            )
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
        }
        val inputCliConfig = HuaweiPublishCliParam(
            deployType = DeployType.DRAFT,
            publishTimeoutMs = "1001",
            publishPeriodMs = "2002",
            credentialsPath = CREDENTIALS_FILE_SECOND_PATH,
            clientId = "id123",
            clientSecret = "secret123",
            buildFormat = BuildFormat.AAB,
            buildFile = ARTIFACT_AAB_FILE_SECOND_PATH,
            releaseTime = "2019-10-18T21:00:00+0300",
            releasePhaseStartTime = "2021-10-18T21:00:00+0300",
            releasePhaseEndTime = "2022-10-18T21:00:00+0300",
            releasePhasePercent = "10.05"
        )
        val configProvider = ConfigProvider(
            extension = inputExtensionConfig,
            cli = inputCliConfig,
            buildFileProvider = buildFileProvider
        )

        val actualValue = configProvider.getConfig()

        assertThat(actualValue).isEqualTo(expectedValue)
    }

    @Test
    fun `correct config with overriding of publish param`() {

        val expected = HuaweiPublishConfig(
            credentials = Credentials("id", "secret"),
            deployType = DeployType.PUBLISH,
            artifactFormat = BuildFormat.APK,
            artifactFile = File(ARTIFACT_APK_FILE_PATH),
            publishTimeoutMs = DEFAULT_PUBLISH_TIMEOUT_MS,
            publishPeriodMs = DEFAULT_PUBLISH_PERIOD_MS,
            releaseTime = null,
            releasePhase = null
        )

        tableOf("expectedValue", "actualValue")
            .row(
                expected.copy(deployType = DeployType.PUBLISH),
                ConfigProvider(
                    extension = extensionConfigInstance().apply {
                        credentialsPath = CREDENTIALS_FILE_PATH
                    },
                    cli = HuaweiPublishCliParam(),
                    buildFileProvider = buildFileProvider
                )
            )
            .row(
                expected.copy(deployType = DeployType.DRAFT),
                ConfigProvider(
                    extension = extensionConfigInstance().apply {
                        credentialsPath = CREDENTIALS_FILE_PATH
                        deployType = DeployType.DRAFT
                    },
                    cli = HuaweiPublishCliParam(),
                    buildFileProvider = buildFileProvider
                )
            )
            .row(
                expected.copy(deployType = DeployType.UPLOAD_ONLY),
                ConfigProvider(
                    extension = extensionConfigInstance().apply {
                        credentialsPath = CREDENTIALS_FILE_PATH
                        deployType = DeployType.UPLOAD_ONLY
                    },
                    cli = HuaweiPublishCliParam(),
                    buildFileProvider = buildFileProvider
                )
            )
            .row(
                expected.copy(deployType = DeployType.DRAFT),
                ConfigProvider(
                    extension = extensionConfigInstance().apply {
                        credentialsPath = CREDENTIALS_FILE_PATH
                        deployType = DeployType.DRAFT
                    },
                    cli = HuaweiPublishCliParam(
                        deployType = null
                    ),
                    buildFileProvider = buildFileProvider
                )
            )
            .row(
                expected.copy(deployType = DeployType.UPLOAD_ONLY),
                ConfigProvider(
                    extension = extensionConfigInstance().apply {
                        credentialsPath = CREDENTIALS_FILE_PATH
                        deployType = DeployType.DRAFT
                    },
                    cli = HuaweiPublishCliParam(
                        deployType = DeployType.UPLOAD_ONLY
                    ),
                    buildFileProvider = buildFileProvider
                )
            )
            .forAll { expectedValue, actualValue ->
                assertThat(actualValue.getConfig()).isEqualTo(expectedValue)
            }
    }
}