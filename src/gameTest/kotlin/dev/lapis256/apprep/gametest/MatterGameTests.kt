package dev.lapis256.apprep.gametest

import dev.lapis256.apprep.common.block.entity.ReplicationConnectorBlockEntity
import dev.lapis256.apprep.common.init.AppRepBlocks
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.neoforge.gametest.GameTestHolder
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate

@GameTestHolder("apprep")
@PrefixGameTestTemplate(false)
class MatterGameTests {
    @GameTest(template = "empty")
    fun connectorPersistence(helper: GameTestHelper) {
        val block = AppRepBlocks.REPLICATION_CONNECTOR.block()
        val itemName = AppRepBlocks.REPLICATION_CONNECTOR.stack().hoverName.contents as? TranslatableContents
        if (itemName?.key != block.descriptionId) return helper.fail("Connector item and block names differ")
        val pos = BlockPos.ZERO
        helper.setBlock(pos, block)
        val entity = helper.getBlockEntity(pos) as? ReplicationConnectorBlockEntity
            ?: return helper.fail("Could not create connector")
        entity.setPriority(42)
        val level = helper.level
        val saved = entity.saveWithFullMetadata(level.registryAccess())
        val restored = BlockEntity.loadStatic(helper.absolutePos(pos), entity.blockState, saved, level.registryAccess())
            as? ReplicationConnectorBlockEntity ?: return helper.fail("Could not restore connector")
        if (restored.priority != 42) return helper.fail("Connector priority did not survive saving")
        helper.runAfterDelay(10) {
            if (entity.matterNetworkElement == null) helper.fail("Connector did not register with the matter network")
            else helper.succeed()
        }
    }
}
