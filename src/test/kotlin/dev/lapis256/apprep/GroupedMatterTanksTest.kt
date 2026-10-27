package dev.lapis256.apprep

import appeng.api.config.Actionable
import com.buuz135.replication.api.MatterType
import com.buuz135.replication.api.matter_fluid.MatterTank
import com.buuz135.replication.api.matter_fluid.MatterStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.common.ae2.storage.GroupedMatterTanks
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GroupedMatterTanksTest {
    @Test
    fun simulatedInsertDoesNotReserveAnEmptyTank() {
        val tank = MatterTank(100)
        val tanks = GroupedMatterTanks { listOf(tank) }
        val earth = MatterKey.of(MatterType.EARTH)
        val ender = MatterKey.of(MatterType.ENDER)

        assertEquals(80, tanks.insert(earth, 80, Actionable.SIMULATE))
        assertTrue(tank.isEmpty)
        assertEquals(80, tanks.insert(ender, 80, Actionable.MODULATE))
        assertEquals(MatterType.ENDER, tank.matter.matterType)
        assertEquals(80.0, tank.matterAmount)
    }

    @Test
    fun distributesMatterAndReusesAnEmptiedTank() {
        val first = MatterTank(100)
        val second = MatterTank(100)
        val tanks = GroupedMatterTanks { listOf(first, second) }
        val earth = MatterKey.of(MatterType.EARTH)
        val ender = MatterKey.of(MatterType.ENDER)

        assertEquals(150, tanks.insert(earth, 150, Actionable.MODULATE))
        assertEquals(150, tanks.extract(earth, 150, Actionable.SIMULATE))
        assertEquals(150.0, first.matterAmount + second.matterAmount)
        assertEquals(150, tanks.extract(earth, 150, Actionable.MODULATE))
        assertEquals(100, tanks.insert(ender, 100, Actionable.MODULATE))
        assertEquals(100, tanks.extract(ender, 100, Actionable.MODULATE))
    }
    @Test
    fun leavesFractionalMatterInPlace() {
        val tank = MatterTank(100)
        val earth = MatterKey.of(MatterType.EARTH)
        tank.fill(MatterStack(MatterType.EARTH, 99.5), FluidAction.EXECUTE)
        val tanks = GroupedMatterTanks { listOf(tank) }

        assertEquals(0, tanks.insert(earth, 1, Actionable.MODULATE))
        assertEquals(99.5, tank.matterAmount)
        assertEquals(99, tanks.extract(earth, 100, Actionable.MODULATE))
        assertEquals(0.5, tank.matterAmount)
        assertEquals(0, tanks.extract(earth, 1, Actionable.MODULATE))
        assertEquals(0.5, tank.matterAmount)
    }
}
