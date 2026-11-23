package dev.lapis256.apprep.data.provider.client

import appeng.core.AppEng
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.extension.ItemDefinition
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.extension.MatterCellDefinition
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGAItems
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.data.ExistingFileHelper

class AppRepItemModelProvider(
    output: PackOutput,
    helper: ExistingFileHelper,
) : ItemModelProvider(output, AppliedReplicaticsAPI.MOD_ID, helper) {
    private val parent = mcLoc("item/generated")
    private val cellLed: ResourceLocation = AppEng.makeId("item/storage_cell_led")

    override fun registerModels() {
        AppRepItems.CELLS.forEach(::cellTexture)

        simpleTexture(AppRepItems.MATTER_CELL_HOUSING)

        simpleTexture(AppRepMEGAItems.SKY_REPLICA_INGOT)
        simpleTexture(AppRepMEGAItems.MEGA_MATTER_CELL_HOUSING)
    }

    private fun cellTexture(cell: MatterCellDefinition) {
        val path = cell.id().path
        withExistingParent(path, parent)
            .texture("layer0", modLoc("item/$path"))
            .texture("layer1", cellLed)
    }

    private fun simpleTexture(item: ItemDefinition<*>) =
        withExistingParent(item.id().path, parent)
            .texture("layer0", modLoc("item/${item.id().path}"))
}
