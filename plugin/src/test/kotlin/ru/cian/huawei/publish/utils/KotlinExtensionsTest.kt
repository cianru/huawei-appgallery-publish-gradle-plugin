package ru.cian.huawei.publish.utils

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class KotlinExtensionsTest {

    @Test
    fun `should return '0s' for value less than 1000 millis`() {
        val expectValue = "0s"
        val actualValue = 300L.toHumanPrettyFormatInterval()
        assertThat(actualValue).isEqualTo(expectValue)
    }

    @Test
    fun `should return '4s'`() {
        val expectValue = "4s"
        val actualValue = (4 * 1000L).toHumanPrettyFormatInterval()
        assertThat(actualValue).isEqualTo(expectValue)
    }

    @Test
    fun `should return '7m'`() {
        val expectValue = "7m"
        val actualValue = (7 * 60 * 1000L).toHumanPrettyFormatInterval()
        assertThat(actualValue).isEqualTo(expectValue)
    }

    @Test
    fun `should return '10h'`() {
        val expectValue = "10h"
        val actualValue = (10 * 60 * 60 * 1000L).toHumanPrettyFormatInterval()
        assertThat(actualValue).isEqualTo(expectValue)
    }

    @Test
    fun `should return '3d'`() {
        val expectValue = "3d"
        val actualValue = (3 * 24 * 60 * 60 * 1000L).toHumanPrettyFormatInterval()
        assertThat(actualValue).isEqualTo(expectValue)
    }

    @Test
    fun `should return '5w'`() {
        val expectValue = "5w"
        val actualValue = (5 * 7 * 24 * 60 * 60 * 1000L).toHumanPrettyFormatInterval()
        assertThat(actualValue).isEqualTo(expectValue)
    }
}