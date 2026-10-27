package dev.lapis256.apprep

import appeng.client.gui.style.ScreenStyle
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.locale.Language
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConnectorLocalizationTest {
    private fun resource(path: String): JsonObject =
        javaClass.classLoader.getResourceAsStream(path)!!.bufferedReader().use { JsonParser.parseReader(it).asJsonObject }

    @Test
    fun connectorTitleHasTranslations() {
        val screen = resource("assets/ae2/screens/apprep/replication_connector.json")
        val key = screen.getAsJsonObject("text").getAsJsonObject("dialog_title")
            .getAsJsonObject("text").get("translate").asString
        for (locale in listOf("en_us", "ja_jp", "ru_ru")) {
            val translations = resource("assets/apprep/lang/$locale.json")
            assertTrue(translations.has(key), "$locale is missing $key")
            assertFalse(translations.get(key).asString.isBlank(), "$locale has an empty $key")
        }
    }
    @Test
    fun screenTitleResolvesThroughLanguageData() {
        val screen = javaClass.classLoader.getResourceAsStream("assets/ae2/screens/apprep/replication_connector.json")!!
            .bufferedReader().use { ScreenStyle.GSON.fromJson(it, ScreenStyle::class.java) }
        val translations = mutableMapOf<String, String>()
        javaClass.classLoader.getResourceAsStream("assets/apprep/lang/en_us.json")!!.use { input ->
            Language.loadFromJson(input) { key, value -> translations[key] = value }
        }
        val previous = Language.getInstance()
        Language.inject(object : Language() {
            override fun getOrDefault(key: String, fallback: String) = translations.getOrDefault(key, fallback)
            override fun has(key: String) = translations.containsKey(key)
            override fun isDefaultRightToLeft() = false
            override fun getVisualOrder(text: FormattedText): FormattedCharSequence = previous.getVisualOrder(text)
        })
        try {
            assertEquals("ME Replication Connector", screen.text.getValue("dialog_title").text.string)
        } finally {
            Language.inject(previous)
        }
    }
}
