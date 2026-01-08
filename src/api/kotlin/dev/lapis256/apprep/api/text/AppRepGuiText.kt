package dev.lapis256.apprep.api.text

import appeng.core.localization.LocalizationEnum
import dev.lapis256.apprep.api.AppliedReplicaticsAPI


enum class AppRepGuiText(private val root: String, private val englishText: String) : LocalizationEnum {
    MATTER("Matter"),
    CREATIVE_TAB("Applied Replicatics")
    ;

    constructor(englishText: String) : this("gui.${AppliedReplicaticsAPI.MOD_ID}", englishText)

    override fun getTranslationKey() = "$root.${this.name.lowercase()}"
    override fun getEnglishText() = englishText
}
