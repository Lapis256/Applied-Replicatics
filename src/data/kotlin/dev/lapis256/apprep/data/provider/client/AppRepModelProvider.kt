package dev.lapis256.apprep.data.provider.client

import appeng.client.item.StorageCellStateTintSource
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.init.AppRepBlocks
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.extension.MatterCellDefinition
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.ModelProvider
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.core.Direction
import net.minecraft.data.PackOutput
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder


class AppRepModelProvider(output: PackOutput) : ModelProvider(output, AppliedReplicaticsAPI.MOD_ID) {
    override fun registerModels(blocks: BlockModelGenerators, items: ItemModelGenerators) {
        blocks.createTrivialCube(AppRepBlocks.REPLICATION_CONNECTOR.block())
        items.generateFlatItem(AppRepItems.MATTER_CELL_HOUSING.asItem(), ModelTemplates.FLAT_ITEM)

        for (cell in AppRepItems.CELLS) {
            registerCellItemModel(cell, items)
            registerDriveCellModel(cell, blocks)
        }
    }

    private fun registerCellItemModel(cell: MatterCellDefinition, items: ItemModelGenerators) {
        val tier = cell.tier.namePrefix.lowercase()
        val model = ModelTemplates.THREE_LAYERED_ITEM.create(
            cell.asItem(),
            TextureMapping()
                .put(TextureSlot.LAYER0, Material(AppliedReplicaticsAPI.rl("item/matter_cell_housing")))
                .put(TextureSlot.LAYER1, Material(Identifier.fromNamespaceAndPath("ae2", "item/storage_cell_led")))
                .put(TextureSlot.LAYER2, Material(AppliedReplicaticsAPI.rl("item/cell_tier/tier_$tier"))),
            items.modelOutput
        )
        items.itemModelOutput.accept(
            cell.asItem(),
            ItemModelUtils.tintedModel(model, ItemModelUtils.constantTint(-1), StorageCellStateTintSource())
        )
    }

    /*
     * Cell model geometry is based on LGPL-3.0 code from MEGACells:
     * https://github.com/62832/MEGACells/blob/3107e5a994aaf219f3bf5c219dea424dfc6185d2/src/data/java/gripe/_90/megacells/datagen/MEGAModelProvider.java#L271-L291
     * See the original project's LICENSE for the LGPL-3.0 license text.
     */
    private fun registerDriveCellModel(cell: MatterCellDefinition, blocks: BlockModelGenerators) {
        val tier = cell.tier.namePrefix.lowercase()
        val offset = ((cell.tier.index - 1) * 2).toFloat()
        val cellTexture = TextureSlot.create("cell")
        val texture = Material(AppliedReplicaticsAPI.rl("block/drive/cells/matter"))
        ExtendedModelTemplateBuilder.builder()
            .ambientOcclusion(false)
            .requiredTextureSlot(cellTexture)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element { element ->
                element.to(6f, 2f, 2f)
                element.face(Direction.NORTH) { it.uvs(0f, offset, 6f, offset + 2f) }
                element.face(Direction.UP) { it.uvs(6f, offset, 0f, offset + 2f) }
                element.face(Direction.DOWN) { it.uvs(6f, offset, 0f, offset + 2f) }
                element.faces { _, face -> face.texture(cellTexture).cullface(Direction.NORTH) }
            }
            .build()
            .create(
                AppliedReplicaticsAPI.rl("block/drive/cells/${tier}_matter_cell"),
                TextureMapping().put(cellTexture, texture).put(TextureSlot.PARTICLE, texture),
                blocks.modelOutput
            )
    }
}
