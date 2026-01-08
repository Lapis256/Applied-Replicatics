package dev.lapis256.apprep.common.ae2

import appeng.api.config.Actionable
import appeng.api.stacks.AEKey
import appeng.api.stacks.GenericStack
import appeng.me.storage.ExternalStorageFacade
import com.buuz135.replication.api.matter_fluid.IMatterHandler
import dev.lapis256.apprep.api.ae2.MatterKeyType
import dev.lapis256.apprep.api.ae2.toGenericStackOrNull


class MatterHandlerExternalStorageFacade(private val handler: IMatterHandler) : ExternalStorageFacade() {

    override fun getSlots(): Int = handler.tanks

    override fun getStackInSlot(slot: Int): GenericStack? {
        val stack = handler.getMatterInTank(slot)
        return stack.toGenericStackOrNull()
    }

    override fun getKeyType() = MatterKeyType

    override fun insertExternal(what: AEKey?, amount: Int, mode: Actionable?): Int {
        TODO("Not yet implemented")
    }

    override fun extractExternal(what: AEKey?, amount: Int, mode: Actionable?): Int {
        TODO("Not yet implemented")
    }

    override fun containsAnyFuzzy(keys: Set<AEKey?>?): Boolean {
        TODO("Not yet implemented")
    }
}
