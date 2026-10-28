package dev.lapis256.apprep.gametest

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.parts.automation.StackWorldBehaviors
import com.buuz135.replication.ReplicationRegistry
import com.buuz135.replication.api.MatterType
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.ae2.stack.MatterKeyType
import dev.lapis256.apprep.common.block.entity.ReplicationConnectorBlockEntity
import dev.lapis256.apprep.common.init.AppRepBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
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
        functions.register("external_matter_storage") { -> Consumer<GameTestHelper> { helper -> testExternalMatterStorage(helper) } }
        functions.register("connector_persistence") { -> Consumer<GameTestHelper> { helper -> testConnector(helper) } }
        functions.register(bus)
    }

    private fun testExternalMatterStorage(helper: GameTestHelper) {
        val pos = BlockPos.ZERO
        val creativePos = pos.east()
        helper.setBlock(pos, ReplicationRegistry.Blocks.MATTER_TANK.getBlock())
        helper.setBlock(creativePos, ReplicationRegistry.Blocks.CREATIVE_MATTER_TANK.getBlock())
        helper.runAfterDelay(10) {
            val level = helper.level
            fun storageAt(relativePos: BlockPos, extractableOnly: Boolean = false) = StackWorldBehaviors.createExternalStorageStrategies(
                level, helper.absolutePos(relativePos), Direction.NORTH
            )[MatterKeyType]?.createWrapper(extractableOnly) {}

            val storage = storageAt(pos) ?: return@runAfterDelay helper.fail("Matter tank was not detected")
            if (storageAt(creativePos) == null) return@runAfterDelay helper.fail("Creative matter tank was not detected")
            val key = MatterKey.of(MatterType.EARTH)
            val source = IActionSource.empty()
            if (storage.insert(key, 50, Actionable.MODULATE, source) != 50L) return@runAfterDelay helper.fail("Could not insert matter")
            if (storage.availableStacks[key] != 50L) return@runAfterDelay helper.fail("Stored matter was not visible")
            if (storageAt(pos, true)?.availableStacks?.get(key) != 50L) return@runAfterDelay helper.fail("Extractable matter was not visible")
            if (storage.extract(key, 20, Actionable.MODULATE, source) != 20L) return@runAfterDelay helper.fail("Could not extract matter")
            if (storage.availableStacks[key] != 30L) return@runAfterDelay helper.fail("Remaining matter was not visible")
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
