package dev.lapis256.apprep.data

import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.data.provider.client.AppRepBlockModelProvider
import dev.lapis256.apprep.data.provider.client.AppRepItemModelProvider
import dev.lapis256.apprep.data.provider.client.AppRepLanguageProvider
import dev.lapis256.apprep.data.provider.server.AppRepRecipeProvider
import dev.lapis256.apprep.data.provider.server.loot.AppRepLootTableProvider
import dev.lapis256.apprep.data.provider.server.tags.AppRepBlockTagsProvider
import dev.lapis256.apprep.data.provider.server.tags.AppRepItemTagsProvider
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.data.event.GatherDataEvent


@Mod(value = AppliedReplicaticsAPI.MOD_ID)
class AppliedReplicaticsData(modBus: IEventBus) {
    init {
        modBus.addListener(this::onGatherData)
    }

    private fun onGatherData(event: GatherDataEvent) {
        val generator = event.generator
        val output = generator.packOutput
        val helper = event.existingFileHelper
        val provider = event.lookupProvider

        generator.addProvider(event.includeClient(), AppRepLanguageProvider(output))
        generator.addProvider(event.includeClient(), AppRepItemModelProvider(output, helper))
        generator.addProvider(event.includeClient(), AppRepBlockModelProvider(output, helper))


        generator.addProvider(event.includeServer(), AppRepRecipeProvider(output, provider))
        generator.addProvider(event.includeServer(), AppRepLootTableProvider(output, provider))

        val blockTags = generator.addProvider(event.includeServer(), AppRepBlockTagsProvider(output, provider, helper)).contentsGetter()
        generator.addProvider(event.includeServer(), AppRepItemTagsProvider(output, provider, blockTags, helper))
    }
}
