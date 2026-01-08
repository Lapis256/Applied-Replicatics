package dev.apprep.apprep.integration.megacells.common

import dev.apprep.apprep.integration.megacells.common.init.AppRepMEGAItems
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod


@Mod(AppliedReplicaticsAPI.MOD_ID)
class MEGACellsIntegration(modBus: IEventBus) {
    init {
        if (ModList.get().isLoaded("megacells")) {
            AppRepMEGAItems.REGISTRY.register(modBus)
        }
    }
}
