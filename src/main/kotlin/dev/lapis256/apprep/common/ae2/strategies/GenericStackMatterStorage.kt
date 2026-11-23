package dev.lapis256.apprep.common.ae2.strategies

import appeng.api.behaviors.GenericInternalInventory
import appeng.api.config.Actionable
import com.buuz135.replication.api.matter_fluid.IMatterHandler
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.ae2.stack.MatterKeyType
import net.minecraftforge.fluids.capability.IFluidHandler

@Suppress("UnstableApiUsage")
class GenericStackMatterStorage(
    val inventory: GenericInternalInventory,
) : IMatterHandler {
    override fun getTanks(): Int = inventory.size()

    private fun getKey(tank: Int): MatterKey? = inventory.getKey(tank) as? MatterKey

    override fun getMatterInTank(tank: Int): MatterStack {
        val matter = getKey(tank) ?: return MatterStack.EMPTY
        return matter.toStack(inventory.getAmount(tank))
    }

    override fun getTankCapacity(tank: Int): Int = inventory.getCapacity(MatterKeyType).coerceIn(0, Int.MAX_VALUE.toLong()).toInt()

    override fun isMatterValid(
        tank: Int,
        stack: MatterStack,
    ): Boolean = tank in 0 until inventory.size() && MatterKey.of(stack)?.let(inventory::isAllowed) == true

    override fun fill(
        stack: MatterStack,
        action: IFluidHandler.FluidAction,
    ): Int {
        val key = MatterKey.of(stack) ?: return 0
        return inventory.insert(0, key, stack.amount.toLong(), Actionable.of(action)).toInt()
    }

    override fun drain(
        resource: MatterStack,
        action: IFluidHandler.FluidAction,
    ): MatterStack {
        if (resource.isEmpty || !resource.isMatterEqual(getMatterInTank(0))) {
            return MatterStack.EMPTY
        }
        return drain(resource.amount, action)
    }

    override fun drain(
        maxDrain: Int,
        action: IFluidHandler.FluidAction,
    ): MatterStack {
        val key = getKey(0) ?: return MatterStack.EMPTY
        val extracted = inventory.extract(0, key, maxDrain.toLong(), Actionable.of(action))

        if (extracted == 0L) {
            return MatterStack.EMPTY
        }

        return key.toStack(extracted)
    }
}
