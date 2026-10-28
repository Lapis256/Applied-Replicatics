package dev.lapis256.apprep.common.util

import appeng.api.config.Actionable
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction

val Actionable.fluidAction: FluidAction
    get() = if (this == Actionable.MODULATE) FluidAction.EXECUTE else FluidAction.SIMULATE

fun FluidAction.toActionable(): Actionable =
    if (this == FluidAction.EXECUTE) Actionable.MODULATE else Actionable.SIMULATE
