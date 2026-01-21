package dev.lapis256.apprep.data.provider.client

import appeng.core.AppEng
import appeng.core.definitions.ItemDefinition
import dev.apprep.apprep.integration.megacells.common.init.AppRepMEGAItems
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.extension.MatterCellDefinition
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.client.model.generators.ItemModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper


class AppRepItemModelProvider(output: PackOutput, helper: ExistingFileHelper) :
    ItemModelProvider(output, AppliedReplicaticsAPI.MOD_ID, helper) {

    private val parent = mcLoc("item/generated")
    private val cellLed: ResourceLocation = AppEng.makeId("item/storage_cell_led")

    override fun registerModels() {
        AppRepItems.CELLS.forEach(::cellTexture)

        simpleTexture(AppRepItems.MATTER_CELL_HOUSING)

        simpleTexture(AppRepMEGAItems.SKY_REPLICA_INGOT)
        simpleTexture(AppRepMEGAItems.MEGA_MATTER_CELL_HOUSING)
    }

    private fun cellTexture(cell: MatterCellDefinition) {
        val isMEGA = cell.tier.index in 6..10
        val megaPrefix = if (isMEGA) "mega_" else ""
        withExistingParent(cell.id().path, parent)
            .texture("layer0", modLoc("item/${megaPrefix}matter_cell_housing"))
            .texture("layer1", cellLed)
            .texture("layer2", modLoc("item/cell_tier/tier_${cell.tier.namePrefix.lowercase()}"))
    }

    private fun simpleTexture(item: ItemDefinition<*>) =
        withExistingParent(item.id().path, parent)
            .texture("layer0", modLoc("item/${item.id().path}"))
}
