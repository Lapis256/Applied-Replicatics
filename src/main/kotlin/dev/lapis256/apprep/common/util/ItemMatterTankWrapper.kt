package dev.lapis256.apprep.common.util

import com.buuz135.replication.ReplicationAttachments
import com.buuz135.replication.ReplicationConfig
import com.buuz135.replication.api.matter_fluid.MatterStack
import com.buuz135.replication.api.matter_fluid.MatterTank
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler


class ItemMatterTankWrapper private constructor(private val stack: ItemStack, private val isCreative: Boolean) {
    // TODO: クリエイティブで NBT をコピーしたタンクにも対応する
    // TODO: 空のタンクがタンクとして認識されない問題の修正

    companion object {
        fun isMatterTank(stack: ItemStack): Boolean {
            val tileData = stack.get(ReplicationAttachments.TILE) ?: return false
            return tileData.contains("tank") || tileData.getCompound("lockableMatterTankBundle").contains("Tank")
        }

        fun of(stack: ItemStack): ItemMatterTankWrapper? {
            val tileData = stack.get(ReplicationAttachments.TILE) ?: return null

            val path = stack.itemHolder.key?.location()?.path ?: ""
            val isCreative = path.contains("creative")
            val wrapper = ItemMatterTankWrapper(stack, isCreative)

            if (tileData.contains("tank")) {
                wrapper.tank.readFromNBT(tileData.getCompound("tank"))
                return wrapper
            }

            if (tileData.contains("lockableMatterTankBundle")) {
                wrapper.tank.readFromNBT(tileData.getCompound("lockableMatterTankBundle").getCompound("Tank"))
                return wrapper
            }

            return null
        }
    }

    private val tank by lazy { MatterTank(ReplicationConfig.MatterTank.CAPACITY) }

    private fun mergeTankData(tankData: CompoundTag): CompoundTag {
        val tileData = stack.get(ReplicationAttachments.TILE) ?: error("ItemStack has no TILE attachment")

        if (tileData.contains("tank")) {
            tileData.getCompound("tank").merge(tankData)
        } else if (tileData.contains("lockableMatterTankBundle")) {
            tileData.getCompound("lockableMatterTankBundle").getCompound("Tank").merge(tankData)
        } else {
            error("ItemStack has no tank data")
        }

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
