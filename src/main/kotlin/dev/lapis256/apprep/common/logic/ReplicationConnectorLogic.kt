package dev.lapis256.apprep.common.logic

import appeng.api.networking.GridFlags
import appeng.api.networking.IGridNode
import appeng.api.networking.IManagedGridNode
import appeng.api.networking.ticking.IGridTickable
import appeng.api.networking.ticking.TickRateModulation
import appeng.api.networking.ticking.TickingRequest
import appeng.api.util.AECableType
import com.buuz135.replication.api.matter_fluid.IMatterTank
import com.buuz135.replication.api.network.IMatterTanksConsumer
import com.buuz135.replication.api.network.IMatterTanksSupplier
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag


class ReplicationConnectorLogic(gridNode: IManagedGridNode, val host: ReplicationConnectorLogicHost) :
    IMatterTanksConsumer,
    IMatterTanksSupplier {

    private var _priority: Int = 0
        set(value) {
            field = value
            host.saveChanges()
        }

    fun setPriority(newValue: Int) {
        _priority = newValue
    }

    val mainNode: IManagedGridNode = gridNode
        .setFlags(GridFlags.REQUIRE_CHANNEL)
        .addService(IGridTickable::class.java, Ticker())

    private class Ticker : IGridTickable {
        override fun getTickingRequest(node: IGridNode): TickingRequest {
            return TickingRequest(5, 120, true) // TODO: 調整する
        }

        override fun tickingRequest(node: IGridNode?, ticksSinceLastCall: Int): TickRateModulation {
            AppliedReplicaticsAPI.LOGGER.debug("Replication Connector Ticker called: ticksSinceLastCall=$ticksSinceLastCall")
            return TickRateModulation.FASTER
        }
    }

    fun getCableConnectionType(dir: Direction?): AECableType {
        return AECableType.SMART
    }

    fun notifyNeighbors() {
        if (this.mainNode.isActive) {
            this.mainNode.ifPresent { grid, node ->
                grid.tickManager.wakeDevice(node)
            }
        }

        this.host.getBlockEntity().invalidateCapabilities()
    }

    fun gridChanged() {
        this.notifyNeighbors()
    }

    fun writeToNBT(tag: CompoundTag, registries: HolderLookup.Provider) {
        tag.putInt("priority", this.priority)
    }

    fun readFromNBT(tag: CompoundTag, registries: HolderLookup.Provider) {
        notifyNeighbors()
        this.priority = tag.getInt("priority")
    }

    // IMatterTanksConsumer / IMatterTanksSupplier

    override fun getTanks(): List<IMatterTank> {
        return listOf() // MaterTankWrapperList() TODO: 実装する
    }

    override fun getPriority(): Int = _priority
}
