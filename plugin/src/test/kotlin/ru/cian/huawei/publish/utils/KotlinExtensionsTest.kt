package ru.cian.huawei.publish.utils

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import org.junit.jupiter.api.Test

class KotlinExtensionsTest {

    @Test
    fun `should return correct human pretty interval string format`() {
        tableOf("expectedValue", "actualValue")
            .row("0s", 300L)
            .row("4s", 4 * 1000L)
            .row("10h", 10 * 60 * 60 * 1000L)
            .row("3d", 3 * 24 * 60 * 60 * 1000L)
            .row("5w", 5 * 7 * 24 * 60 * 60 * 1000L)
            .forAll { expectedValue, timeInterval ->
                val actualValue = timeInterval.toHumanPrettyFormatInterval()
                assertThat(actualValue).isEqualTo(expectedValue)
            }
    }
}