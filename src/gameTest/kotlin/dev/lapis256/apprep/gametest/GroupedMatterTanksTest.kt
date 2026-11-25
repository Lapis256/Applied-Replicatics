package dev.lapis256.apprep.gametest

import appeng.api.config.Actionable
import com.buuz135.replication.api.MatterType
import com.buuz135.replication.api.matter_fluid.MatterStack
import com.buuz135.replication.api.matter_fluid.MatterTank
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.common.ae2.storage.GroupedMatterTanks
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import net.minecraftforge.gametest.GameTestHolder
import net.minecraftforge.gametest.PrefixGameTestTemplate

@GameTestHolder("apprep")
@PrefixGameTestTemplate(false)
class GroupedMatterTanksTest {
    @GameTest(template = "empty")
    fun simulatedInsertDoesNotReserveAnEmptyTank(helper: GameTestHelper) {
        val tank = MatterTank(100)
        val tanks = GroupedMatterTanks { listOf(tank) }
        val earth = MatterKey.of(MatterType.EARTH)
        val ender = MatterKey.of(MatterType.ENDER)

        check(tanks.insert(earth, 80, Actionable.SIMULATE) == 80L)
        check(tank.isEmpty)
        check(tanks.insert(ender, 80, Actionable.MODULATE) == 80L)
        check(tank.matter.matterType == MatterType.ENDER)
        check(tank.matterAmount == 80)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun distributesMatterAndReusesAnEmptiedTank(helper: GameTestHelper) {
        val first = MatterTank(100)
        val second = MatterTank(100)
        val tanks = GroupedMatterTanks { listOf(first, second) }
        val earth = MatterKey.of(MatterType.EARTH)
        val ender = MatterKey.of(MatterType.ENDER)

        check(tanks.insert(earth, 150, Actionable.MODULATE) == 150L)
        check(tanks.extract(earth, 150, Actionable.SIMULATE) == 150L)
        check(first.matterAmount + second.matterAmount == 150)
        check(tanks.extract(earth, 150, Actionable.MODULATE) == 150L)
        check(tanks.insert(ender, 100, Actionable.MODULATE) == 100L)
        check(tanks.extract(ender, 100, Actionable.MODULATE) == 100L)
        helper.succeed()
    }

    @GameTest(template = "empty")
    fun respectsFullTankCapacity(helper: GameTestHelper) {
        val tank = MatterTank(100)
        val earth = MatterKey.of(MatterType.EARTH)
        tank.fill(MatterStack(MatterType.EARTH, 100), FluidAction.EXECUTE)
        val tanks = GroupedMatterTanks { listOf(tank) }

        check(tanks.insert(earth, 1, Actionable.MODULATE) == 0L)
        check(tank.matterAmount == 100)
        check(tanks.extract(earth, 100, Actionable.MODULATE) == 100L)
        check(tank.matterAmount == 0)
        check(tanks.extract(earth, 1, Actionable.MODULATE) == 0L)
        check(tank.matterAmount == 0)
        helper.succeed()
    }
}
