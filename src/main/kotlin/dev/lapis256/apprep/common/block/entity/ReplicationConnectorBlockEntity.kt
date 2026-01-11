package dev.lapis256.apprep.common.block.entity

import appeng.api.networking.GridHelper
import appeng.api.networking.IGridNode
import appeng.api.networking.IGridNodeListener
import appeng.api.networking.IManagedGridNode
import appeng.api.util.AECableType
import appeng.blockentity.grid.AENetworkedBlockEntity
import appeng.helpers.IPriorityHost
import appeng.me.helpers.BlockEntityNodeListener
import com.buuz135.replication.network.DefaultMatterNetworkElement
import com.hrznstudio.titanium.block_network.NetworkManager
import dev.lapis256.apprep.api.extension.takeIfServer
import dev.lapis256.apprep.common.init.AppRepBlocks
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogic
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogicHost
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState


class ReplicationConnectorBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    AENetworkedBlockEntity(type, pos, state),
    IPriorityHost,
    ReplicationConnectorLogicHost {

    override fun onLoad() {
        super.onLoad()
        val level = level.takeIfServer() ?: return

        val networkManager = NetworkManager.get(level)
        val existing = networkManager.getElement(worldPosition)
        if (existing != null) {
            logic.addNetworkElementListener(existing)
        } else {
            val newElement = DefaultMatterNetworkElement(level, worldPosition)
            logic.addNetworkElementListener(newElement)
            networkManager.addElement(newElement)
        }
    }

    private var isChunkUnloaded = false
    override fun onChunkUnloaded() {
        isChunkUnloaded = true
    }

    override fun setRemoved() {
        super.setRemoved()

        // チャンクがアンロードされた場合は NetworkElement の削除は行わない
        if (isChunkUnloaded) {
            return
        }

        val element = matterNetworkElement ?: return
        val network = matterNetwork ?: return

        logic.removeNetworkElementListener(element)
        logic.removeMatterNetworkListener(network)

        val level = level.takeIfServer() ?: return
        val networkManager = NetworkManager.get(level)
        networkManager.removeElement(worldPosition)

        network.removeElement(element)
    }

    private object NodeListener : BlockEntityNodeListener<ReplicationConnectorBlockEntity>() {
        override fun onGridChanged(nodeOwner: ReplicationConnectorBlockEntity, node: IGridNode) {
            nodeOwner.logic.gridChanged()
        }
    }

    override fun createMainNode(): IManagedGridNode {
        return GridHelper.createManagedNode(this, NodeListener)
    }

    override fun onMainNodeStateChanged(reason: IGridNodeListener.State?) {
        super<ReplicationConnectorLogicHost>.onMainNodeStateChanged()
        if (mainNode.hasGridBooted()) {
            logic.notifyNeighbors()
        }
    }

    override fun saveAdditional(data: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(data, registries)
        logic.writeToNBT(data, registries)
    }

    override fun loadTag(data: CompoundTag, registries: HolderLookup.Provider) {
        super.loadTag(data, registries)
        logic.readFromNBT(data, registries)
    }

    override fun getCableConnectionType(dir: Direction?): AECableType {
        return logic.getCableConnectionType(dir)
    }

    // ReplicationConnectorLogicHost

    override val logic = ReplicationConnectorLogic(mainNode, this)

    // IPriorityHost

    override fun getMainMenuIcon(): ItemStack = AppRepBlocks.REPLICATION_CONNECTOR.stack()
}
