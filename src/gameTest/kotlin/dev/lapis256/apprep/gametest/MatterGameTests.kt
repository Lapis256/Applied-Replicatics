package dev.lapis256.apprep.gametest

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.KeyCounter
import appeng.api.storage.StorageCells
import appeng.blockentity.storage.DriveBlockEntity
import appeng.core.definitions.AEBlocks
import appeng.core.definitions.AEItems
import com.buuz135.replication.ReplicationRegistry
import com.buuz135.replication.api.IMatterType
import com.buuz135.replication.api.MatterType
import com.buuz135.replication.api.matter_fluid.MatterStack
import com.buuz135.replication.block.tile.DisintegratorBlockEntity
import com.buuz135.replication.block.tile.MatterTankBlockEntity
import com.buuz135.replication.network.MatterNetwork
import com.hrznstudio.titanium.block_network.NetworkManager
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.replication.task.MEReplicationTask
import dev.lapis256.apprep.api.replication.task.internalMatterStacks
import dev.lapis256.apprep.api.replication.util.MATTER_TYPES
import dev.lapis256.apprep.common.block.entity.ReplicationConnectorBlockEntity
import dev.lapis256.apprep.common.init.AppRepBlocks
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.common.util.ItemMatterTankWrapper
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import net.minecraftforge.gametest.GameTestHolder
import net.minecraftforge.gametest.PrefixGameTestTemplate

@GameTestHolder("apprep")
@PrefixGameTestTemplate(false)
class MatterGameTests {
    @GameTest(template = "empty")
    fun exportsNewMatterFromDisintegratorToME(helper: GameTestHelper) {
        val connectorPos = BlockPos.ZERO
        val drivePos = connectorPos.east()
        val disintegratorPos = connectorPos.above()
        helper.setBlock(connectorPos, AppRepBlocks.REPLICATION_CONNECTOR.block())
        helper.setBlock(drivePos, AEBlocks.DRIVE.block())
        helper.setBlock(drivePos.east(), AEBlocks.CREATIVE_ENERGY_CELL.block())
        helper.setBlock(
            disintegratorPos,
            ReplicationRegistry.Blocks.DISINTEGRATOR.left
                .get(),
        )
        val connector = helper.getBlockEntity(connectorPos) as ReplicationConnectorBlockEntity
        val drive = helper.getBlockEntity(drivePos) as DriveBlockEntity
        drive.internalInventory.setItemDirect(0, ItemStack(AppRepItems.MATTER_CELL_1K))
        val disintegrator = helper.getBlockEntity(disintegratorPos) as DisintegratorBlockEntity
        helper.runAfterDelay(20) {
            val network = checkNotNull(connector.matterNetwork)
            val element = checkNotNull(NetworkManager.get(helper.level).getElement(disintegrator.blockPos))
            if (element.network !== network) {
                (element.network as? MatterNetwork)?.removeElement(element)
                element.joinNetwork(network)
                network.update(helper.level)
            }

            val tanks = connector.tanks
            check(tanks.size > 1 && tanks.all { it.matter.isEmpty }) { "Expected several empty connector tanks" }
            val type =
                MATTER_TYPES.firstOrNull { type ->
                    val sample = MatterStack(type, 40)
                    tanks.first().fill(sample, FluidAction.SIMULATE) == 0 &&
                        tanks.drop(1).any { it.fill(sample, FluidAction.SIMULATE) == 40 }
                } ?: error("Expected an empty ME cell that accepts a type other than the first tank's type")
            check(disintegrator.matterTankComponents.first().fillForced(MatterStack(type, 40), FluidAction.EXECUTE) == 40) {
                "Could not fill the disintegrator's output tank"
            }
            val cellInventory = checkNotNull(drive.getOriginalCellInventory(0))
            helper.succeedWhen {
                val amount = KeyCounter().also(cellInventory::getAvailableStacks)[MatterKey.of(type)]
                helper.assertTrue(amount == 40L, "Expected 40 new Matter in the ME cell, got $amount")
            }
        }
    }

    @GameTest(template = "empty")
    fun connectorPersistence(helper: GameTestHelper) {
        val block = AppRepBlocks.REPLICATION_CONNECTOR.block()
        val itemName =
            AppRepBlocks.REPLICATION_CONNECTOR
                .stack()
                .hoverName.contents as? TranslatableContents
        if (itemName?.key != block.descriptionId) return helper.fail("Connector item and block names differ")
        val pos = BlockPos.ZERO
        helper.setBlock(pos, block)
        val entity =
            helper.getBlockEntity(pos) as? ReplicationConnectorBlockEntity
                ?: return helper.fail("Could not create connector")
        entity.setPriority(42)
        val saved = entity.saveWithFullMetadata()
        val restored =
            BlockEntity.loadStatic(helper.absolutePos(pos), entity.blockState, saved)
                as? ReplicationConnectorBlockEntity ?: return helper.fail("Could not restore connector")
        if (restored.priority != 42) return helper.fail("Connector priority did not survive saving")
        helper.runAfterDelay(10) {
            if (entity.matterNetworkElement == null) {
                helper.fail("Connector did not register with the matter network")
            } else {
                helper.succeed()
            }
        }
    }

    @GameTest(template = "empty")
    fun extractsMatterAfterNativeTankFill(helper: GameTestHelper) {
        val tankPos = BlockPos(0, 1, 0)
        helper.setBlock(BlockPos.ZERO, AppRepBlocks.REPLICATION_CONNECTOR.block())
        helper.setBlock(
            tankPos,
            ReplicationRegistry.Blocks.MATTER_TANK.left
                .get(),
        )
        val connector = helper.getBlockEntity(BlockPos.ZERO) as ReplicationConnectorBlockEntity
        val entity = helper.getBlockEntity(tankPos) as MatterTankBlockEntity
        helper.runAfterDelay(10) {
            val network = checkNotNull(connector.matterNetwork)
            val element = checkNotNull(NetworkManager.get(helper.level).getElement(entity.blockPos))
            if (element.network !== network) {
                (element.network as? MatterNetwork)?.removeElement(element)
                element.joinNetwork(network)
                network.update(helper.level)
            }

            val storage = checkNotNull(connector.logic.delegatingStorage.storage)
            val source = IActionSource.empty()
            val earth = MatterKey.of(MatterType.EARTH)
            val tank = entity.tanks.single()
            check(storage.extract(earth, 40, Actionable.SIMULATE, source) == 0L)
            check(KeyCounter().also(storage::getAvailableStacks)[earth] == 0L)
            check(tank.fill(MatterStack(MatterType.EARTH, 40), FluidAction.EXECUTE) == 40)
            val listed = KeyCounter().also(storage::getAvailableStacks)[earth]
            val extracted = storage.extract(earth, 40, Actionable.MODULATE, source)
            check(listed == 40L) { "Expected 40 Matter in storage, got $listed" }
            check(extracted == 40L) { "Expected to extract 40 Matter after filling the native tank, got $extracted" }
            helper.succeed()
        }
    }

    @GameTest(template = "empty")
    fun fillingKeepsOriginalTankUnchanged(helper: GameTestHelper) {
        for (locked in listOf(false, true)) {
            val stack =
                ItemStack(
                    ReplicationRegistry.Blocks.MATTER_TANK.left
                        .get(),
                )
            val tankData = MatterStack(MatterType.EARTH, 20).writeToNBT(CompoundTag())
            stack.addTagElement(
                "Tile",
                CompoundTag().apply {
                    if (locked) {
                        put("lockableMatterTankBundle", CompoundTag().apply { put("Tank", tankData) })
                    } else {
                        put("tank", tankData)
                    }
                },
            )
            val wrapper = requireNotNull(ItemMatterTankWrapper.of(stack))
            if (wrapper.fill(MatterStack(MatterType.EARTH, 10), FluidAction.EXECUTE) != 10) return helper.fail("Wrong fill amount")
            val updated = wrapper.getChangedStack()
            if (ItemMatterTankWrapper.of(stack)?.matter?.amount != 20) return helper.fail("Original tank changed")
            if (ItemMatterTankWrapper.of(updated)?.matter?.amount != 30) return helper.fail("Updated tank has the wrong amount")
        }
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun replicationTaskPersistsInternalMatter(helper: GameTestHelper) {
        val output = requireNotNull(AEItemKey.of(Items.STONE))
        val matter = Object2LongOpenHashMap<IMatterType>().apply { put(MatterType.EARTH, 16L) }
        val task = MEReplicationTask.create(matter, output, 1, BlockPos.ZERO)
        val restored = MEReplicationTask.create(Object2LongOpenHashMap(), output, 1, BlockPos.ZERO)

        restored.deserializeNBT(task.serializeNBT())

        if (restored.internalMatterStacks.getLong(MatterType.EARTH) != 16L) {
            return helper.fail("Replication task did not preserve its internal matter")
        }
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun disassemblesOnlyEmptyMatterCells(helper: GameTestHelper) {
        val player = helper.makeMockSurvivalPlayer()
        player.isShiftKeyDown = true
        val slot = player.inventory.selected
        val stack = ItemStack(AppRepItems.MATTER_CELL_1K)
        player.inventory.setItem(slot, stack)

        val cell = requireNotNull(StorageCells.getCellInventory(stack, null))
        val matter = MatterKey.of(MatterType.EARTH)
        val source = IActionSource.ofPlayer(player)
        check(cell.insert(matter, 1L, Actionable.MODULATE, source) == 1L)

        stack.item.use(helper.level, player, InteractionHand.MAIN_HAND)
        check(player.inventory.getItem(slot) === stack)
        check(cell.extract(matter, 1L, Actionable.MODULATE, source) == 1L)

        stack.item.use(helper.level, player, InteractionHand.MAIN_HAND)
        check(player.inventory.items.none { it.item == AppRepItems.MATTER_CELL_1K.asItem() })
        check(player.inventory.items.sumOf { if (it.item == AEItems.CELL_COMPONENT_1K.asItem()) it.count else 0 } == 1)
        check(player.inventory.items.sumOf { if (it.item == AppRepItems.MATTER_CELL_HOUSING.asItem()) it.count else 0 } == 1)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun matterKeyPacketRoundTrip(helper: GameTestHelper) {
        val key = MatterKey.of(MatterType.EARTH)
        val buffer = FriendlyByteBuf(Unpooled.buffer())
        try {
            key.writeToPacket(buffer)
            check(MatterKey.fromPacket(buffer) == key)
            check(!buffer.isReadable)
        } finally {
            buffer.release()
        }
        helper.succeed()
    }
}
