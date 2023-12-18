package ru.cian.huawei.publish.utils

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.mockkObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HtmlTagsTest {

    @Test
    fun `remote html tags from text`() = mockkObject(CredentialHelper) {

        val sourceValue = """
            <en-US>
                <h1>Check out App Rada website.r</h1>
                Check out<a href=”https://appradar.com/blog/how-to-use-html-emoji-in-google-play-store-app-listing”>Release notes with tags sample</a>webiste.
                Colored <font color=”red”>red text</font>.
                
                <h2>Second   chapter.t</h21>
                Add linebreak to move <br/> on new row.
            
                <h1>Languages of the web</h1>
                <h3>&#x2022; HTML</h3>
                <h3>&#x2022; CSS</h3>
                <h3>&#x2022; JavaScript</h3>
                <h3>&#x2022; PHP</h3>
            </en-US>            
        """.trimIndent()

        val expectedValue = "" +
            "Check out App Rada website.r\n" +
            "Check outRelease notes with tags samplewebiste.\n" +
            "Colored red text.\n" +
            "\n" +
            "Second chapter.t\n" +
            "Add linebreak to move on new row.\n" +
            "\n" +
            "Languages of the web\n" +
            "* HTML\n" +
            "* CSS\n" +
            "* JavaScript\n" +
            "* PHP"

        val actualValue = sourceValue
            // remove html tags
            .replace("\\<[^>]*>".toRegex(), "")
            // remove html symbols
            .replace("(&#)[^;]*;".toRegex(), "*")
            // compress all non-newline whitespaces to single space
            .replace("[\\s&&[^\\n]]+".toRegex(), " ")
            // 2. remove spaces from begining or end of lines
//            .replace("(?m)^\\s|\\s$".toRegex(), "")
//            // 3. compress multiple newlines to single newlines
//            .replace("\\n+".toRegex(), "\n")
//            // 4. remove newlines from begining or end of string
//            .replace("^\n|\n$".toRegex(), "")
            .trimIndent()

        println("actualValue=$actualValue")

        assertThat(actualValue).isEqualTo(expectedValue)
    }
}