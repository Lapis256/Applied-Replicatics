package dev.lapis256.apprep.common.util

import com.buuz135.replication.ReplicationAttachments
import com.buuz135.replication.ReplicationConfig
import com.buuz135.replication.api.matter_fluid.MatterStack
import com.buuz135.replication.api.matter_fluid.MatterTank
import com.buuz135.replication.block.CreativeMatterTankBlock
import com.buuz135.replication.block.MatterTankBlock
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler


class ItemMatterTankWrapper private constructor(private val stack: ItemStack, private val isCreative: Boolean) {
    companion object {
        fun isMatterTank(stack: ItemStack): Boolean {
            val blockItem = stack.item as? BlockItem ?: return false
            if (blockItem.block is MatterTankBlock || blockItem.block is CreativeMatterTankBlock) {
                return true
            }
            val data = stack.getOrDefault(ReplicationAttachments.TILE, CompoundTag())
            return data.contains("tank") || data.getCompound("lockableMatterTankBundle").contains("Tank")
        }

        fun of(stack: ItemStack): ItemMatterTankWrapper? {
            val blockItem = stack.item as? BlockItem ?: return null
            val isCreative = blockItem.block is CreativeMatterTankBlock
            if (!(blockItem.block is MatterTankBlock || isCreative)) {
                return null
            }

            val wrapper = ItemMatterTankWrapper(stack, isCreative)
            val data = stack.getOrDefault(ReplicationAttachments.TILE, CompoundTag())

            if (data.contains("lockableMatterTankBundle")) {
                wrapper.tank.readFromNBT(data.getCompound("lockableMatterTankBundle").getCompound("Tank"))
                return wrapper
            }

            wrapper.tank.readFromNBT(data.getCompound("tank"))
            return wrapper
        }
    }

    private val tank by lazy { MatterTank(ReplicationConfig.MatterTank.CAPACITY) }

    private fun mergeTankData(tankData: CompoundTag): CompoundTag {
        val tileData = stack.get(ReplicationAttachments.TILE) ?: CompoundTag()

        if (tileData.contains("lockableMatterTankBundle")) {
            tileData.getCompound("lockableMatterTankBundle").getCompound("Tank").merge(tankData)
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
        newStack.set(ReplicationAttachments.TILE, tileData)
        return newStack
    }

    fun fill(resource: MatterStack, action: IFluidHandler.FluidAction) = tank.fill(resource, action)

    fun drain(resource: MatterStack, action: IFluidHandler.FluidAction): MatterStack {
        if (resource.isEmpty || !resource.isMatterEqual(matter)) {
            return MatterStack.EMPTY
        }

        return drain(resource.amount, action)
    }

    fun drain(maxDrain: Double, action: IFluidHandler.FluidAction): MatterStack {
        if (isCreative) {
            return MatterStack(matter, maxDrain)
        }

        return tank.drain(maxDrain, action)
    }

    val isEmpty: Boolean get() = tank.isEmpty
    val matter: MatterStack get() = tank.matter
}
