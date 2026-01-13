package dev.lapis256.apprep.api.ae2.util

import appeng.api.stacks.GenericStack
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.ae2.stack.MatterKey


fun MatterStack.toGenericStackOrNull(): GenericStack? {
    val key = MatterKey.of(this) ?: return null
    return GenericStack(key, amount.toLong())
}
