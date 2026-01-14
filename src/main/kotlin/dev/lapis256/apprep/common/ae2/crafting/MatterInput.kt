package dev.lapis256.apprep.common.ae2.crafting

import appeng.api.crafting.IPatternDetails
import appeng.api.stacks.AEKey
import appeng.api.stacks.GenericStack
import com.buuz135.replication.calculation.ReplicationCalculation
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import kotlin.math.roundToLong


class MatterInput(private val stack: GenericStack, private val amount: Long) : IPatternDetails.IInput {
    constructor(key: MatterKey, amount: Long) : this(GenericStack(key, 1), amount)

    companion object {
        fun calculateFromOutput(output: ItemStack): Array<MatterInput> {
            val compound = ReplicationCalculation.getMatterCompound(output) ?: return arrayOf()
            val inputs = compound.values.values.map {
                MatterInput(MatterKey.of(it.matter), it.amount.roundToLong())
            }.toTypedArray()
            return inputs
        }
    }

    private val inputs = arrayOf(stack)
    override fun getPossibleInputs(): Array<GenericStack> = inputs

    override fun getMultiplier() = amount

    override fun isValid(input: AEKey, level: Level?): Boolean = input.matches(stack)

    override fun getRemainingKey(template: AEKey?): AEKey? = null
}
