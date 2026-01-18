package dev.lapis256.apprep.data.provider.server.loot

import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import java.util.concurrent.CompletableFuture


class AppRepLootTableProvider(output: PackOutput, provider: CompletableFuture<HolderLookup.Provider>) :
    LootTableProvider(output, emptySet(), SUB_PROVIDERS, provider) {

    companion object {
        val SUB_PROVIDERS = listOf(
            SubProviderEntry(::AppRepBlockLootSubProvider, LootContextParamSets.BLOCK)
        )
    }
}
