package dev.lapis256.apprep.common.util

import appeng.api.config.Actionable
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction

internal fun transferWholeMatter(amount: Long, mode: Actionable, transfer: (Long, FluidAction) -> Double): Long {
    val transferable = transfer(amount, FluidAction.SIMULATE).toLong().coerceIn(0, amount)
    if (transferable == 0L || mode == Actionable.SIMULATE) return transferable
    // shortcut: simulation cannot guarantee atomicity; use transactions if Replication exposes them.
    return transfer(transferable, FluidAction.EXECUTE).toLong()
}
