package dev.lapis256.apprep

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import com.buuz135.replication.api.MatterType
import com.buuz135.replication.api.matter_fluid.MatterTank
import com.buuz135.replication.api.matter_fluid.IMatterHandler
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.common.ae2.strategies.MatterHandlerExternalStorageFacade
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction

class MatterHandlerExternalStorageFacadeTest {
    @Test
    fun exposesAndTransfersMatter() {
        val tank = MatterTank(100)
        val storage = MatterHandlerExternalStorageFacade(tank)
        val earth = MatterKey.of(MatterType.EARTH)
        val ender = MatterKey.of(MatterType.ENDER)
        val source = IActionSource.empty()
        var changes = 0
        storage.setChangeListener { changes++ }

        assertEquals(75, storage.insert(earth, 75, Actionable.SIMULATE, source))
        assertTrue(tank.isEmpty)
        assertEquals(75, storage.insert(earth, 75, Actionable.MODULATE, source))
        assertEquals(75, storage.availableStacks[earth])
        assertTrue(storage.containsAnyFuzzy(setOf(earth)))
        assertFalse(storage.containsAnyFuzzy(setOf(ender)))
        assertEquals(0, storage.extract(ender, 20, Actionable.MODULATE, source))
        assertEquals(20, storage.extract(earth, 20, Actionable.SIMULATE, source))
        assertEquals(75.0, tank.matterAmount)
        assertEquals(20, storage.extract(earth, 20, Actionable.MODULATE, source))
        assertEquals(55, storage.availableStacks[earth])
        assertEquals(2, changes)
    }

    @Test
    fun extractableOnlyHidesMatterThatCannotBeExtracted() {
        val tank = MatterTank(100)
        tank.fill(MatterStack(MatterType.EARTH, 60.0), FluidAction.EXECUTE)
        val handler = object : IMatterHandler by tank {
            override fun drain(resource: MatterStack, action: FluidAction): MatterStack = MatterStack.EMPTY
        }
        val storage = MatterHandlerExternalStorageFacade(handler)
        val key = MatterKey.of(MatterType.EARTH)

        assertEquals(60, storage.availableStacks[key])
        storage.setExtractableOnly(true)
        assertEquals(0, storage.availableStacks[key])
    }
}
