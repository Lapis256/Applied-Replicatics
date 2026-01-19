package dev.lapis256.apprep.common.storage

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEKey
import appeng.api.stacks.GenericStack
import appeng.api.storage.MEStorage
import appeng.helpers.externalstorage.GenericStackInv
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level


class ReplicationConnectorReturnInventory(listener: Runnable) : GenericStackInv(listener, SLOT_SIZE) {
    companion object {
        const val SLOT_SIZE = 9
    }

    override fun getMaxAmount(key: AEKey?): Long = Long.MAX_VALUE

    fun addDrops(level: Level, pos: BlockPos, drops: MutableList<ItemStack>) {
        stacks.asSequence().filterNotNull().forEach { stack ->
            stack.what().addDrops(stack.amount(), drops, level, pos)
        }
    }

    override fun canExtract(): Boolean = false

    fun returnIntoStorage(storage: MEStorage, source: IActionSource): Boolean {
        var didSomething = false
        stacks
            .asSequence()
            .filterNotNull()
            .forEachIndexed { i, stack ->
                val sizeBefore = stack.amount()
                val inserted = storage.insert(stack.what(), sizeBefore, Actionable.MODULATE, source)
                if (inserted > 0L) {
                    didSomething = true
                }

                stacks[i] = if (inserted < sizeBefore) {
                    GenericStack(stack.what(), sizeBefore - inserted)
                } else {
                    null
                }
            }

        return didSomething
    }
}
