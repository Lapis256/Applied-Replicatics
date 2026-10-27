package dev.lapis256.apprep

import com.buuz135.replication.ReplicationAttachments
import com.buuz135.replication.ReplicationRegistry
import com.buuz135.replication.api.MatterType
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.common.util.ItemMatterTankWrapper
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction
import net.neoforged.testframework.junit.EphemeralTestServerProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(EphemeralTestServerProvider::class)
class ItemMatterTankWrapperTest {
    @Test
    fun fillingKeepsOriginalStackUnchanged(server: MinecraftServer) {
        for (locked in listOf(false, true)) {
            val stack = ItemStack(ReplicationRegistry.Blocks.MATTER_TANK)
            val tankData = MatterStack(MatterType.EARTH, 20.0).writeToNBT(CompoundTag())
            stack.set(ReplicationAttachments.TILE.get(), CompoundTag().apply {
                if (locked) put("lockableMatterTankBundle", CompoundTag().apply { put("Tank", tankData) })
                else put("tank", tankData)
            })
            val wrapper = requireNotNull(ItemMatterTankWrapper.of(stack))
            assertEquals(10.0, wrapper.fill(MatterStack(MatterType.EARTH, 10.0), FluidAction.EXECUTE))
            val updated = wrapper.getChangedStack()
            assertEquals(20.0, ItemMatterTankWrapper.of(stack)?.matter?.amount)
            assertEquals(30.0, ItemMatterTankWrapper.of(updated)?.matter?.amount)
        }
    }
}
