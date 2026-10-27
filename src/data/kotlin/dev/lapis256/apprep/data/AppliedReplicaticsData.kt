package dev.lapis256.apprep.data

import dev.lapis256.apprep.api.AppliedReplicaticsAPI
// import dev.lapis256.apprep.data.provider.client.AppRepBlockModelProvider
import dev.lapis256.apprep.data.provider.client.AppRepItemModelProvider
import dev.lapis256.apprep.data.provider.client.AppRepLanguageProvider
import dev.lapis256.apprep.data.provider.server.AppRepRecipeProvider
import dev.lapis256.apprep.data.provider.server.loot.AppRepLootTableProvider
import dev.lapis256.apprep.data.provider.server.tags.AppRepBlockTagsProvider
// import dev.lapis256.apprep.data.provider.server.tags.AppRepItemTagsProvider
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.data.event.GatherDataEvent


@Mod(value = AppliedReplicaticsAPI.MOD_ID)
class AppliedReplicaticsData(modBus: IEventBus) {
    init {
        modBus.addListener(this::onGatherData)
    }

    private fun onGatherData(event: GatherDataEvent.Client) {
        event.createProvider(::AppRepLanguageProvider)
        event.createProvider(::AppRepItemModelProvider)
        event.createProvider { output, provider -> AppRepRecipeProvider.Runner(output, provider) }
        event.createProvider(::AppRepLootTableProvider)
        event.createProvider(::AppRepBlockTagsProvider)
    }
}
