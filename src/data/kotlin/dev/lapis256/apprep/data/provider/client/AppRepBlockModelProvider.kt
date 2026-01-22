package dev.lapis256.apprep.data.provider.client

import appeng.datagen.providers.models.AE2BlockStateProvider
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGABlocks
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.init.AppRepBlocks
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.extension.MatterCellDefinition
import net.minecraft.core.Direction
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.ExistingFileHelper


class AppRepBlockModelProvider(output: PackOutput, helper: ExistingFileHelper) :
    AE2BlockStateProvider(output, AppliedReplicaticsAPI.MOD_ID, helper) {

    override fun registerStatesAndModels() {
        AppRepItems.CELLS.forEach(::matterCell)

        simpleBlockAndItem(AppRepBlocks.REPLICATION_CONNECTOR)

        simpleBlockAndItem(AppRepMEGABlocks.SKY_REPLICA_BLOCK)
    }

    private fun matterCell(drive: MatterCellDefinition) {
        val tier = drive.tier
        val isMEGA = tier.index in 6..10
        cell(
            "${tier.namePrefix()}_matter_cell",
            if (isMEGA) "mega_matter" else "matter",
            (tier.index() - (if (isMEGA) 6 else 1)) * 2,
        )
    }

    /*
     * This method is based on the following LGPL-3.0 code:
     * https://github.com/62832/MEGACells/blob/3107e5a994aaf219f3bf5c219dea424dfc6185d2/src/data/java/gripe/_90/megacells/datagen/MEGAModelProvider.java#L271-L291
     *
     * For the full text of the LGPL-3.0 License, please refer to the above URL or the LICENSE file of the original project.
     */
    private fun cell(cell: String, texture: String, offset: Int) {
        val texturePrefix = "block/drive/cells/"
        models().getBuilder(texturePrefix + cell)
            .ao(false)
            .texture("cell", texturePrefix + texture)
            .texture("particle", texturePrefix + texture)
            .element()
            .to(6f, 2f, 2f)
            .face(Direction.NORTH)
            .uvs(0f, offset.toFloat(), 6f, (offset + 2).toFloat())
            .end()
            .face(Direction.UP)
            .uvs(6f, offset.toFloat(), 0f, (offset + 2).toFloat())
            .end()
            .face(Direction.DOWN)
            .uvs(6f, offset.toFloat(), 0f, (offset + 2).toFloat())
            .end()
            .faces { _, builder ->
                builder.texture("#cell").cullface(Direction.NORTH).end()
            }
            .end()
    }
}
