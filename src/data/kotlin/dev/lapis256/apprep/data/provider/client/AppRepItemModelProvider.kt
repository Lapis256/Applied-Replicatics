package dev.lapis256.apprep.data.provider.client

import appeng.client.item.StorageCellStateTintSource
import com.google.gson.JsonParser
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.init.AppRepBlocks
import dev.lapis256.apprep.common.init.AppRepItems
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.ModelProvider
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.data.models.model.ModelInstance
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.data.PackOutput
import net.minecraft.resources.Identifier

class AppRepItemModelProvider(output: PackOutput) : ModelProvider(output, AppliedReplicaticsAPI.MOD_ID) {
    override fun registerModels(blocks: BlockModelGenerators, items: ItemModelGenerators) {
        blocks.createTrivialCube(AppRepBlocks.REPLICATION_CONNECTOR.block())
        items.generateFlatItem(AppRepItems.MATTER_CELL_HOUSING.asItem(), ModelTemplates.FLAT_ITEM)

        for (cell in AppRepItems.CELLS) {
            val tier = cell.tier.namePrefix.lowercase()
            val model = ModelTemplates.THREE_LAYERED_ITEM.create(
                cell.asItem(),
                TextureMapping()
                    .put(TextureSlot.LAYER0, Material(AppliedReplicaticsAPI.rl("item/matter_cell_housing")))
                    .put(TextureSlot.LAYER1, Material(Identifier.fromNamespaceAndPath("ae2", "item/storage_cell_led")))
                    .put(TextureSlot.LAYER2, Material(AppliedReplicaticsAPI.rl("item/cell_tier/tier_$tier"))),
                items.modelOutput
            )
            items.itemModelOutput.accept(cell.asItem(), ItemModelUtils.tintedModel(model, ItemModelUtils.constantTint(-1), StorageCellStateTintSource()))

            val offset = (cell.tier.index - 1) * 2
            val json = """{"ambientocclusion":false,"elements":[{"from":[0,0,0],"to":[6,2,2],"faces":{"north":{"uv":[0,$offset,6,${offset + 2}],"texture":"#cell","cullface":"north"},"up":{"uv":[6,$offset,0,${offset + 2}],"texture":"#cell","cullface":"north"},"down":{"uv":[6,$offset,0,${offset + 2}],"texture":"#cell","cullface":"north"}}}],"textures":{"cell":"apprep:block/drive/cells/matter","particle":"apprep:block/drive/cells/matter"}}"""
            blocks.modelOutput.accept(AppliedReplicaticsAPI.rl("block/drive/cells/${tier}_matter_cell"), ModelInstance { JsonParser.parseString(json) })
        }
    }
}
