package dev.lapis256.apprep.integration.appliedflux.common

import appeng.api.upgrades.Upgrades
import com.glodblock.github.appflux.common.AFSingletons
import com.glodblock.github.appflux.common.me.service.IEnergyDistributor
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.connector.ReplicationConnectorExtensions
import dev.lapis256.apprep.api.connector.ReplicationConnectorUpgrades
import dev.lapis256.apprep.common.init.AppRepBlocks
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent


@Mod(AppliedReplicaticsAPI.MOD_ID)
class AppliedFluxIntegration(eventBus: IEventBus) {
    init {
        if (ModList.get().isLoaded(MOD_ID)) {
            ReplicationConnectorUpgrades.addSlots(1)
            ReplicationConnectorExtensions
                .registerNodeService<IEnergyDistributor>(::ReplicationEnergyDistributor)
            eventBus.addListener(::onCommonSetup)
        }
    }

    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        event.enqueueWork {
            Upgrades.add(AFSingletons.INDUCTION_CARD, AppRepBlocks.REPLICATION_CONNECTOR, 1)
        }
    }

    companion object {
        const val MOD_ID = "appflux"
    }
}
