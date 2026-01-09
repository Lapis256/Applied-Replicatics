package dev.lapis256.apprep.common.parts

import appeng.api.networking.GridHelper
import appeng.api.networking.IGridNode
import appeng.api.networking.IGridNodeListener
import appeng.api.networking.IManagedGridNode
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartItem
import appeng.api.util.AECableType
import appeng.parts.AEBasePart
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogic
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogicHost
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack


class ReplicationConnectorPart(partItem: IPartItem<*>) : AEBasePart(partItem), ReplicationConnectorLogicHost {
    override fun getBoxes(helper: IPartCollisionHelper) {
        helper.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0)
        helper.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 14.0)
    }

    override fun saveChanges() {
        host.markForSave()
    }

    override fun readFromNBT(data: CompoundTag, registries: HolderLookup.Provider) {
        super.readFromNBT(data, registries)
        logic.readFromNBT(data, registries)
    }

    override fun writeToNBT(data: CompoundTag, registries: HolderLookup.Provider) {
        super.writeToNBT(data, registries)
        logic.writeToNBT(data, registries)
    }

    override fun getCableConnectionLength(cable: AECableType?): Float = 4.0F

    override fun onMainNodeStateChanged(reason: IGridNodeListener.State?) {
        super.onMainNodeStateChanged(reason)
        if (mainNode.hasGridBooted()) {
            this.logic.notifyNeighbors()
        }
    }

    private object PartNodeListener : NodeListener<ReplicationConnectorPart>() {
        override fun onGridChanged(nodeOwner: ReplicationConnectorPart, node: IGridNode) {
            nodeOwner.logic.gridChanged()
        }
    }

    override fun createMainNode(): IManagedGridNode {
        return GridHelper.createManagedNode(this, PartNodeListener)
    }

    // ReplicationConnectorLogicHost

    override val logic = ReplicationConnectorLogic(mainNode, this)

    // IPriorityHost

    override fun getMainMenuIcon(): ItemStack = ItemStack(partItem)
}
