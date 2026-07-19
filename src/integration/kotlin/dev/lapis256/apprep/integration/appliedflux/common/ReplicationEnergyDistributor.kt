package dev.lapis256.apprep.integration.appliedflux.common

import appeng.api.config.Actionable
import com.buuz135.replication.network.MatterNetwork
import com.glodblock.github.appflux.common.AFSingletons
import com.glodblock.github.appflux.common.me.energy.EnergyTickRecord
import com.glodblock.github.appflux.common.me.key.FluxKey
import com.glodblock.github.appflux.common.me.key.type.EnergyType
import com.glodblock.github.appflux.common.me.service.EnergyDistributeService
import com.glodblock.github.appflux.common.me.service.IEnergyDistributor
import com.glodblock.github.appflux.config.AFConfig
import dev.lapis256.apprep.api.connector.ReplicationConnectorExtension
import dev.lapis256.apprep.api.connector.ReplicationConnectorExtensionContext


class ReplicationEnergyDistributor(
    private val context: ReplicationConnectorExtensionContext,
) : IEnergyDistributor, ReplicationConnectorExtension {
    companion object {
        private val FLUX_KEY = FluxKey.of(EnergyType.FE)
    }

    private val tickRecord = EnergyTickRecord()

    private var service: EnergyDistributeService? = null
    private var matterNetwork: MatterNetwork? = null

    override fun setServiceHost(service: EnergyDistributeService?) {
        this.service = service
        updateSleepingState()
    }

    override fun isActive(): Boolean =
        context.managedNode.isActive &&
            context.upgrades.isInstalled(AFSingletons.INDUCTION_CARD) &&
            context.matterNetwork != null

    override fun distribute(tickCounter: Long) {
        val matterNetwork = context.matterNetwork ?: return
        if (this.matterNetwork !== matterNetwork) {
            this.matterNetwork = matterNetwork
        }
        if (!tickRecord.needTick(tickCounter)) {
            return
        }

        val inserted = transferEnergy(matterNetwork)
        tickRecord.sent(inserted)
        if (inserted > 0) {
            context.managedNode.node?.level?.let(matterNetwork::markDirty)
        }
    }

    private fun transferEnergy(matterNetwork: MatterNetwork): Long {
        val ioLimit = AFConfig.getFluxAccessorIO().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val receivable = matterNetwork.energyStorage.receiveEnergy(ioLimit, true)
        val limit = minOf(receivable, ioLimit)
        if (limit == 0) {
            return 0
        }

        val storage = context.managedNode.grid?.storageService?.inventory ?: return 0

        val extracted = storage.extract(FLUX_KEY, limit.toLong(), Actionable.MODULATE, context.actionSource)
        if (extracted == 0L) {
            return 0
        }

        val inserted = matterNetwork.energyStorage.receiveEnergy(extracted.toInt(), false)
        val remainder = extracted - inserted.toLong()
        if (remainder > 0) {
            storage.insert(FLUX_KEY, remainder, Actionable.MODULATE, context.actionSource)
        }

        return inserted.toLong()
    }

    override fun onUpgradesChanged() {
        updateSleepingState()
    }

    private fun updateSleepingState() {
        val service = service ?: return
        if (context.upgrades.isInstalled(AFSingletons.INDUCTION_CARD)) {
            service.wake(this)
        } else {
            service.sleep(this)
        }
    }
}
