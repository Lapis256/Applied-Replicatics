package dev.lapis256.apprep.gametest

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.KeyCounter
import com.buuz135.replication.api.MatterType
import com.buuz135.replication.block.tile.MatterTankBlockEntity
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.common.block.entity.ReplicationConnectorBlockEntity
import dev.lapis256.apprep.common.init.AppRepBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.neoforge.gametest.GameTestHolder
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction

@GameTestHolder("apprep")
@PrefixGameTestTemplate(false)
class MatterGameTests {
    @GameTest(template = "empty")
    fun extractsMatterAfterNativeTankFill(helper: GameTestHelper) {
        val tankPos = BlockPos.ZERO.above()
        helper.setBlock(BlockPos.ZERO, AppRepBlocks.REPLICATION_CONNECTOR.block())
        helper.setBlock(tankPos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("replication", "matter_tank")))
        val connector = helper.getBlockEntity(BlockPos.ZERO) as ReplicationConnectorBlockEntity
        val entity = helper.getBlockEntity(tankPos) as MatterTankBlockEntity

        helper.runAfterDelay(10) {
            val storage = checkNotNull(connector.logic.delegatingStorage.storage)
            val source = IActionSource.empty()
            val earth = MatterKey.of(MatterType.EARTH)
            val tank = entity.tanks.single()
            check(storage.extract(earth, 40, Actionable.SIMULATE, source) == 0L)
            check(KeyCounter().also(storage::getAvailableStacks)[earth] == 0L)
            tank.fill(earth.toStack(40), FluidAction.EXECUTE)
            val listed = KeyCounter().also(storage::getAvailableStacks)[earth]
            val extracted = storage.extract(earth, 40, Actionable.MODULATE, source)
            check(listed == 40L) { "Expected 40 Matter in storage, got $listed" }
            check(extracted == 40L) { "Expected to extract 40 Matter after filling the native tank, got $extracted" }
            helper.succeed()
        }
    }

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
