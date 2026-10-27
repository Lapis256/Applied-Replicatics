package dev.lapis256.apprep

import com.google.gson.JsonObject
import com.google.gson.JsonParser
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
}
