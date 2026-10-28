package dev.lapis256.apprep.gametest

import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.block.entity.ReplicationConnectorBlockEntity
import dev.lapis256.apprep.common.init.AppRepBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Consumer


@Mod(AppliedReplicaticsAPI.MOD_ID)
class MatterGameTests(bus: IEventBus) {
    init {
        val functions = DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, AppliedReplicaticsAPI.MOD_ID)
        functions.register("connector_persistence") { -> Consumer<GameTestHelper> { helper -> testConnector(helper) } }
        functions.register(bus)
    }

    private fun testConnector(helper: GameTestHelper) {
        val block = AppRepBlocks.REPLICATION_CONNECTOR.block()
        val itemName = AppRepBlocks.REPLICATION_CONNECTOR.stack().hoverName.contents as? TranslatableContents
        if (itemName?.key != block.descriptionId) return helper.fail("Connector item and block names differ")
        val relativePos = BlockPos.ZERO
        helper.setBlock(relativePos, AppRepBlocks.REPLICATION_CONNECTOR.block())
        val entity = helper.getBlockEntity(relativePos, ReplicationConnectorBlockEntity::class.java)
        entity.setPriority(42)
        val level = helper.level
        val saved = entity.saveWithFullMetadata(level.registryAccess())
        val restored = BlockEntity.loadStatic(helper.absolutePos(relativePos), entity.blockState, saved, level.registryAccess())
            as? ReplicationConnectorBlockEntity ?: return helper.fail("Could not restore connector")
        if (restored.priority != 42) return helper.fail("Connector priority did not survive saving")
        helper.runAfterDelay(10) {
            if (entity.matterNetworkElement == null) helper.fail("Connector did not register with the matter network")
            else helper.succeed()
        }
    }
}
