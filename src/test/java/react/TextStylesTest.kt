package react

import org.junit.jupiter.api.Test
import pw.mihou.nexus.features.react.styles.TextStyles
import kotlin.test.assertEquals

class TextStylesTest: TextStyles {
    companion object {
        const val PLACEHOLDER_TEXT = "Hello World"
    }

    @Test
    fun `test stacked styling`() {
        val result = p(PLACEHOLDER_TEXT, bold = true, underline = true, italic = true, strikethrough = true, spoiler = true)
        assertEquals("||***__~~Hello World~~__***||", result)
    }
}