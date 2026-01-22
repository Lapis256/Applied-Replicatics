package dev.lapis256.apprep.integration.emi.client

import appeng.api.integrations.emi.EmiStackConverter
import appeng.api.stacks.GenericStack
import com.buuz135.replication.api.IMatterType
import dev.emi.emi.api.stack.EmiStack
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.ae2.util.toGenericStackOrNull


object MatterStackConverter : EmiStackConverter {
    override fun getKeyType(): Class<*> = IMatterType::class.java

    override fun toEmiStack(stack: GenericStack): EmiStack? {
        val key = stack.what as? MatterKey ?: return null
        return MatterEmiStack(key.stack, stack.amount)
    }

    override fun toGenericStack(stack: EmiStack): GenericStack? {
        val matterEmiStack = stack as? MatterEmiStack ?: return null
        return matterEmiStack.stack.toGenericStackOrNull()
    }
}
