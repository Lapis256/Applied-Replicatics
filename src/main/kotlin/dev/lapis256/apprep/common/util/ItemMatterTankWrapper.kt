package dev.lapis256.apprep.common.util

import com.buuz135.replication.api.matter_fluid.MatterStack
import com.buuz135.replication.api.matter_fluid.MatterTank
import com.buuz135.replication.block.MatterTankBlock
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fluids.capability.IFluidHandler

class ItemMatterTankWrapper private constructor(
    private val stack: ItemStack,
) {
    companion object {
        fun isMatterTank(stack: ItemStack): Boolean {
            val blockItem = stack.item as? BlockItem ?: return false
            if (blockItem.block is MatterTankBlock) {
                return true
            }
            val data = stack.getTagElement("Tile") ?: CompoundTag()
            return data.contains("tank") || data.getCompound("lockableMatterTankBundle").contains("Tank")
        }

        fun of(stack: ItemStack): ItemMatterTankWrapper? {
            val blockItem = stack.item as? BlockItem ?: return null
            if (blockItem.block !is MatterTankBlock) {
                return null
            }

            val wrapper = ItemMatterTankWrapper(stack)
            val data = stack.getTagElement("Tile") ?: CompoundTag()

            if (data.contains("lockableMatterTankBundle")) {
                wrapper.tank.readFromNBT(data.getCompound("lockableMatterTankBundle").getCompound("Tank"))
                return wrapper
            }

            wrapper.tank.readFromNBT(data.getCompound("tank"))
            return wrapper
        }
    }

    private val tank by lazy { MatterTank(256_000) }

    private fun mergeTankData(tankData: CompoundTag): CompoundTag {
        val tileData = stack.getTagElement("Tile")?.copy() ?: CompoundTag()

        if (tileData.contains("lockableMatterTankBundle")) {
            val bundle = tileData.getCompound("lockableMatterTankBundle")
            bundle.put("Tank", bundle.getCompound("Tank").merge(tankData))
            tileData.put("lockableMatterTankBundle", bundle)
            return tileData
        }

        tileData.put("tank", tankData)
        return tileData
    }

    fun getChangedStack(): ItemStack {
        val newStack = stack.copy()
        val tankData = CompoundTag()
        tank.writeToNBT(tankData)
        val tileData = mergeTankData(tankData)
        newStack.addTagElement("Tile", tileData)
        return newStack
    }

    fun fill(
        resource: MatterStack,
        action: IFluidHandler.FluidAction,
    ) = tank.fill(resource, action)

    fun drain(
        resource: MatterStack,
        action: IFluidHandler.FluidAction,
    ): MatterStack {
        if (resource.isEmpty || !resource.isMatterEqual(matter)) {
            return MatterStack.EMPTY
        }

        return drain(resource.amount, action)
    }

    fun drain(
        maxDrain: Int,
        action: IFluidHandler.FluidAction,
    ): MatterStack = tank.drain(maxDrain, action)

    val isEmpty: Boolean get() = tank.isEmpty
    val matter: MatterStack get() = tank.matter
}
