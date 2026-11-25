package dev.lapis256.apprep.data.provider.server.loot

import net.minecraft.data.PackOutput
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets

class AppRepLootTableProvider(
    output: PackOutput,
) : LootTableProvider(output, emptySet(), SUB_PROVIDERS) {
    companion object {
        val SUB_PROVIDERS =
            listOf(
                SubProviderEntry(::AppRepBlockLootSubProvider, LootContextParamSets.BLOCK),
            )
    }
}
