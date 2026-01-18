package dev.apprep.apprep.integration.megacells.common

import dev.apprep.apprep.integration.megacells.common.init.AppRepMEGABlocks
import dev.apprep.apprep.integration.megacells.common.init.AppRepMEGAItems
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod


@Mod(AppliedReplicaticsAPI.MOD_ID)
class MEGACellsIntegration(modBus: IEventBus) {
    companion object {
        const val MOD_ID = "megacells"
        val isLoaded: Boolean by lazy { ModList.get().isLoaded(MOD_ID) }
    }

    init {
        if (isLoaded) {
            AppRepMEGABlocks.REGISTRY.register(modBus)
            AppRepMEGAItems.REGISTRY.register(modBus)
        }
    }
}
