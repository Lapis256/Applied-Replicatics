package dev.lapis256.apprep.gametest

import com.buuz135.replication.ReplicationAttachments
import com.buuz135.replication.ReplicationRegistry
import com.buuz135.replication.api.MatterType
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.util.ItemMatterTankWrapper
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Consumer

@Mod(AppliedReplicaticsAPI.MOD_ID)
class MatterGameTests(bus: IEventBus) {
    init {
        val functions = DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, AppliedReplicaticsAPI.MOD_ID)
        functions.register("tank_item") { -> Consumer<GameTestHelper> { helper -> testTankItem(helper) } }
        functions.register(bus)
    }

    private fun testTankItem(helper: GameTestHelper) {
        for (locked in listOf(false, true)) {
            val stack = ItemStack(ReplicationRegistry.Blocks.MATTER_TANK)
            val tankData = MatterStack(MatterType.EARTH, 20.0).writeToNBT(CompoundTag())
            stack.set(ReplicationAttachments.TILE.get(), CompoundTag().apply {
                if (locked) put("lockableMatterTankBundle", CompoundTag().apply { put("Tank", tankData) })
                else put("tank", tankData)
            })
            val wrapper = ItemMatterTankWrapper.of(stack) ?: return helper.fail("Not a matter tank")
            if (wrapper.fill(MatterStack(MatterType.EARTH, 10.0), FluidAction.EXECUTE) != 10.0) {
                return helper.fail("Could not fill tank")
            }
            val updated = wrapper.getChangedStack()
            if (ItemMatterTankWrapper.of(stack)?.matter?.amount != 20.0) return helper.fail("Original tank changed")
            if (ItemMatterTankWrapper.of(updated)?.matter?.amount != 30.0) return helper.fail("Updated tank did not retain matter")
        }
        helper.succeed()
    }
}
