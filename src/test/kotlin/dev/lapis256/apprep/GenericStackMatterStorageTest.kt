package dev.lapis256.apprep

import appeng.helpers.externalstorage.GenericStackInv
import com.buuz135.replication.api.MatterType
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.ae2.stack.MatterKeyType
import dev.lapis256.apprep.common.ae2.strategies.GenericStackMatterStorage
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class GenericStackMatterStorageTest {
    @Test
    fun fillReturnsInsertedAmountAndRespectsSimulation() {
        val inventory = GenericStackInv({}, 1).apply { setCapacity(MatterKeyType, 100) }
        val storage = GenericStackMatterStorage(inventory)
        val matter = MatterStack(MatterType.EARTH, 150.0)

        assertEquals(100.0, storage.fill(matter, FluidAction.SIMULATE))
        assertTrue(storage.getMatterInTank(0).isEmpty)
        assertEquals(100.0, storage.fill(matter, FluidAction.EXECUTE))
        assertEquals(100.0, storage.getMatterInTank(0).amount)
    }

    @Test
    fun drainRejectsDifferentMatter() {
        val inventory = GenericStackInv({}, 1).apply { setCapacity(MatterKeyType, 100) }
        val storage = GenericStackMatterStorage(inventory)
        storage.fill(MatterStack(MatterType.EARTH, 60.0), FluidAction.EXECUTE)

        assertTrue(storage.drain(MatterStack(MatterType.ENDER, 20.0), FluidAction.EXECUTE).isEmpty)
        assertEquals(60.0, storage.getMatterInTank(0).amount)
        assertEquals(20.0, storage.drain(MatterStack(MatterType.EARTH, 20.0), FluidAction.EXECUTE).amount)
    }
}
