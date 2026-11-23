package dev.lapis256.apprep.gametest

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.KeyCounter
import com.buuz135.replication.api.MatterType
import com.buuz135.replication.block.tile.MatterTankBlockEntity
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.common.block.entity.ReplicationConnectorBlockEntity
import dev.lapis256.apprep.common.init.AppRepBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Consumer


@Mod(AppliedReplicaticsAPI.MOD_ID)
class MatterGameTests(bus: IEventBus) {
    init {
        val functions = DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, AppliedReplicaticsAPI.MOD_ID)
        functions.register("connector_persistence") { -> Consumer<GameTestHelper> { helper -> testConnector(helper) } }
        functions.register("extracts_matter_after_native_tank_fill") { ->
            Consumer<GameTestHelper>(::extractsMatterAfterNativeTankFill)
        }
        functions.register(bus)
    }

    private fun extractsMatterAfterNativeTankFill(helper: GameTestHelper) {
        val tankPos = BlockPos.ZERO.above()
        val tankBlock = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("replication", "matter_tank"))
        helper.setBlock(BlockPos.ZERO, AppRepBlocks.REPLICATION_CONNECTOR.block())
        helper.setBlock(tankPos, tankBlock)
        val connector = helper.getBlockEntity(BlockPos.ZERO, ReplicationConnectorBlockEntity::class.java)
        val entity = helper.getBlockEntity(tankPos, MatterTankBlockEntity::class.java)

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
