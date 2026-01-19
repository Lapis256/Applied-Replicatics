package dev.lapis256.apprep.data.provider.client

import dev.apprep.apprep.integration.megacells.common.init.AppRepMEGABlocks
import dev.apprep.apprep.integration.megacells.common.init.AppRepMEGAItems
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.text.AppRepGuiText
import dev.lapis256.apprep.common.init.AppRepBlocks
import dev.lapis256.apprep.common.init.AppRepItems
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider


class AppRepLanguageProvider(output: PackOutput) : LanguageProvider(output, AppliedReplicaticsAPI.MOD_ID, "en_us") {
    override fun addTranslations() {
        for (item in AppRepItems.ITEMS + AppRepMEGAItems.ITEMS) {
            add(item.asItem(), item.englishName)
        }

        for (block in AppRepBlocks.BLOCKS + AppRepMEGABlocks.BLOCKS) {
            add(block.asItem(), block.englishName)
        }

        for(text in AppRepGuiText.entries) {
            add(text.translationKey, text.englishText)
        }
    }
}
