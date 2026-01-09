package dev.lapis256.apprep.common.logic

import appeng.helpers.IPriorityHost
import appeng.menu.ISubMenu
import com.buuz135.replication.api.matter_fluid.IMatterTank
import com.buuz135.replication.api.network.IMatterTanksConsumer
import com.buuz135.replication.api.network.IMatterTanksSupplier
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.entity.BlockEntity


interface ReplicationConnectorLogicHost : IMatterTanksConsumer, IMatterTanksSupplier, IPriorityHost {

    fun getBlockEntity(): BlockEntity

    fun saveChanges()

    val logic: ReplicationConnectorLogic

    // IMatterTanksConsumer / IMatterTanksSupplier

    override fun getTanks(): List<IMatterTank> = logic.tanks

    override fun getPriority(): Int = logic.priority

    // IPriorityHost

    override fun setPriority(newValue: Int) {
        logic.priority = newValue
    }

    override fun returnToMainMenu(player: Player?, subMenu: ISubMenu?) {
        TODO("Not yet implemented")
    }
}
