package dev.lapis256.apprep.common.ae2.strategies

import appeng.api.config.Actionable
import appeng.api.stacks.AEKey
import appeng.api.stacks.GenericStack
import appeng.api.stacks.KeyCounter
import appeng.core.localization.GuiText
import appeng.me.storage.ExternalStorageFacade
import com.buuz135.replication.api.matter_fluid.IMatterHandler
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.ae2.stack.MatterKeyType
import dev.lapis256.apprep.api.ae2.util.toGenericStackOrNull
import dev.lapis256.apprep.common.util.transferWholeMatter
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction


class MatterHandlerExternalStorageFacade(private val handler: IMatterHandler) : ExternalStorageFacade() {

    override fun getSlots(): Int = handler.tanks

    override fun getStackInSlot(slot: Int): GenericStack? {
        val stack = handler.getMatterInTank(slot)
        return stack.toGenericStackOrNull()
    }

    override fun getKeyType() = MatterKeyType

    override fun getDescription() = GuiText.ExternalStorage.text(MatterKeyType.description)

    override fun insertExternal(what: AEKey?, amount: Int, mode: Actionable?): Int {
        val key = what as? MatterKey ?: return 0
        return transferWholeMatter(amount.toLong(), mode ?: return 0) { count, action ->
            handler.fill(key.toStack(count), action)
        }.toInt()
    }

    override fun extractExternal(what: AEKey?, amount: Int, mode: Actionable?): Int {
        val key = what as? MatterKey ?: return 0
        return transferWholeMatter(amount.toLong(), mode ?: return 0) { count, action ->
            handler.drain(key.toStack(count), action).amount
        }.toInt()
    }

    override fun containsAnyFuzzy(keys: Set<AEKey?>?): Boolean {
        return (0 until handler.tanks).any { slot ->
            getStackInSlot(slot)?.what()?.dropSecondary() in keys.orEmpty()
        }
    }

    override fun getAvailableStacks(out: KeyCounter) {
        for (slot in 0 until handler.tanks) {
            val matter = handler.getMatterInTank(slot)
            val stack = matter.toGenericStackOrNull() ?: continue
            if (extractableOnly && handler.drain(matter, FluidAction.SIMULATE).isEmpty) continue
            out.add(stack.what(), stack.amount())
        }
    }
}
