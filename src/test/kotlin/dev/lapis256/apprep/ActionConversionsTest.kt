package dev.lapis256.apprep

import appeng.api.config.Actionable
import dev.lapis256.apprep.common.util.fluidAction
import dev.lapis256.apprep.common.util.toActionable
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ActionConversionsTest {
    @Test
    fun preservesExecutionAndSimulation() {
        assertEquals(FluidAction.EXECUTE, Actionable.MODULATE.fluidAction)
        assertEquals(FluidAction.SIMULATE, Actionable.SIMULATE.fluidAction)
        assertEquals(Actionable.MODULATE, FluidAction.EXECUTE.toActionable())
        assertEquals(Actionable.SIMULATE, FluidAction.SIMULATE.toActionable())
    }
}
