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
        val generator = event.generator
        val output = generator.packOutput
        val provider = event.lookupProvider

        generator.addProvider(true, AppRepLanguageProvider(output))
        generator.addProvider(true, AppRepItemModelProvider(output))
        // generator.addProvider(true, AppRepBlockModelProvider(output, helper))


        generator.addProvider(true, AppRepRecipeProvider.Runner(output, provider))
        generator.addProvider(true, AppRepLootTableProvider(output, provider))

        generator.addProvider(true, AppRepBlockTagsProvider(output, provider))
        // generator.addProvider(true, AppRepItemTagsProvider(output, provider, blockTags, helper))
    }
}
