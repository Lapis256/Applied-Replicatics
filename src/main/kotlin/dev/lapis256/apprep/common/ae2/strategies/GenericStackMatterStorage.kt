package dev.lapis256.apprep.common.ae2.strategies

import appeng.api.behaviors.GenericInternalInventory
import appeng.api.config.Actionable
import com.buuz135.replication.api.matter_fluid.IMatterHandler
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.ae2.MatterKey
import dev.lapis256.apprep.api.ae2.MatterKeyType
import net.neoforged.neoforge.fluids.capability.IFluidHandler


@Suppress("UnstableApiUsage")
class GenericStackMatterStorage(val inventory: GenericInternalInventory) : IMatterHandler {

    override fun getTanks(): Int = inventory.size()

    private fun getKey(tank: Int): MatterKey? {
        return inventory.getKey(tank) as? MatterKey
    }

    override fun getMatterInTank(tank: Int): MatterStack {
        val matter = getKey(tank) ?: return MatterStack.EMPTY
        return matter.toStack(inventory.getAmount(tank))
    }

    override fun getTankCapacity(tank: Int): Double =
        inventory.getCapacity(MatterKeyType).toDouble()

    override fun isMatterValid(tank: Int, stack: MatterStack): Boolean =
        inventory.isAllowedIn(tank, MatterKey.of(stack))

    override fun fill(stack: MatterStack, action: IFluidHandler.FluidAction): Double {
        val key = MatterKey.of(stack) ?: return 0.0
        val remainder = stack.amount - inventory.insert(0, key, stack.amount.toLong(), Actionable.of(action))

        if (remainder <= 0.0) {
            return 0.0
        }

        return key.toStack(remainder.toLong()).amount
    }

    override fun drain(resource: MatterStack, action: IFluidHandler.FluidAction): MatterStack {
        if (resource.isEmpty || !resource.isMatterEqual(resource)) {
            return MatterStack.EMPTY
        }
        return drain(resource.amount, action)
    }

    override fun drain(maxDrain: Double, action: IFluidHandler.FluidAction): MatterStack {
        val key = getKey(0) ?: return MatterStack.EMPTY
        val extracted = inventory.extract(0, key, maxDrain.toLong(), Actionable.of(action))

        if (extracted == 0L) {
            return MatterStack.EMPTY
        }

        return key.toStack(extracted)
    }
}
