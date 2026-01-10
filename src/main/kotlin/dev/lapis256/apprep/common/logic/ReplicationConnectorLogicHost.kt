package dev.lapis256.apprep.common.logic

import appeng.helpers.IPriorityHost
import appeng.menu.ISubMenu
import com.buuz135.replication.api.matter_fluid.IMatterTank
import com.buuz135.replication.api.network.IMatterTanksConsumer
import com.buuz135.replication.api.network.IMatterTanksSupplier
import com.buuz135.replication.network.DefaultMatterNetworkElement
import com.buuz135.replication.network.MatterNetwork
import com.hrznstudio.titanium.block_network.NetworkManager
import dev.lapis256.apprep.api.extension.takeIfServer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.entity.BlockEntity


interface ReplicationConnectorLogicHost : IMatterTanksConsumer, IMatterTanksSupplier, IPriorityHost {

    fun getBlockEntity(): BlockEntity?

    fun saveChanges()

    val logic: ReplicationConnectorLogic

    val matterNetworkElement: DefaultMatterNetworkElement?
        get() {
            val entity = getBlockEntity() ?: return null
            val level = entity.level.takeIfServer() ?: return null
            return NetworkManager.get(level).getElement(entity.blockPos) as? DefaultMatterNetworkElement
        }

    val matterNetwork: MatterNetwork?
        get() = matterNetworkElement?.getNetwork() as? MatterNetwork

    fun onMainNodeStateChanged() {
        logic.onMainNodeStateChanged()
    }

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
