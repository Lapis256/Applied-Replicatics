package dev.lapis256.apprep.gametest

import appeng.helpers.externalstorage.GenericStackInv
import com.buuz135.replication.api.MatterType
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.ae2.stack.MatterKeyType
import dev.lapis256.apprep.common.ae2.strategies.GenericStackMatterStorage
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import net.minecraftforge.gametest.GameTestHolder
import net.minecraftforge.gametest.PrefixGameTestTemplate

@GameTestHolder("apprep")
@PrefixGameTestTemplate(false)
class GenericStackMatterStorageTest {
    @GameTest(template = "empty")
    fun fillReturnsInsertedAmountAndRespectsSimulation(helper: GameTestHelper) {
        val inventory = GenericStackInv({}, 1).apply { setCapacity(MatterKeyType, 100) }
        val storage = GenericStackMatterStorage(inventory)
        val matter = MatterStack(MatterType.EARTH, 150)

        check(storage.fill(matter, FluidAction.SIMULATE) == 100)
        check(storage.getMatterInTank(0).isEmpty)
        check(storage.fill(matter, FluidAction.EXECUTE) == 100)
        check(storage.getMatterInTank(0).amount == 100)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun drainRejectsDifferentMatter(helper: GameTestHelper) {
        val inventory = GenericStackInv({}, 1).apply { setCapacity(MatterKeyType, 100) }
        val storage = GenericStackMatterStorage(inventory)
        storage.fill(MatterStack(MatterType.EARTH, 60), FluidAction.EXECUTE)

        check(storage.drain(MatterStack(MatterType.ENDER, 20), FluidAction.EXECUTE).isEmpty)
        check(storage.getMatterInTank(0).amount == 60)
        check(storage.drain(MatterStack(MatterType.EARTH, 20), FluidAction.EXECUTE).amount == 20)
        helper.succeed()
    }
}
