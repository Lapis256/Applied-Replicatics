package dev.apprep.apprep.integration.jei.client

import appeng.api.stacks.GenericStack
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.ae2.util.toGenericStackOrNull
import tamaized.ae2jeiintegration.api.integrations.jei.IngredientConverter
import kotlin.math.max


class MatterIngredientConverter : IngredientConverter<MatterStack> {
    override fun getIngredientType() = AppRepJEI.TYPE_MATTER

    override fun getIngredientFromStack(genericStack: GenericStack): MatterStack? {
        val stack = genericStack.what as? MatterKey ?: return null
        return stack.toStack(max(1L, genericStack.amount))
    }

    override fun getStackFromIngredient(stack: MatterStack): GenericStack? =
        stack.toGenericStackOrNull()
}
