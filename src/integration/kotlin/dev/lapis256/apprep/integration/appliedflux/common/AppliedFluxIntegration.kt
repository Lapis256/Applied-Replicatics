package dev.lapis256.apprep.integration.appliedflux.common

import appeng.api.upgrades.Upgrades
import com.glodblock.github.appflux.common.AFItemAndBlock
import com.glodblock.github.appflux.common.me.service.IEnergyDistributor
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.connector.ReplicationConnectorExtensions
import dev.lapis256.apprep.api.connector.ReplicationConnectorUpgrades
import dev.lapis256.apprep.common.init.AppRepBlocks
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent

@Mod.EventBusSubscriber(modid = AppliedReplicaticsAPI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
object AppliedFluxIntegration {
    @JvmStatic
    @SubscribeEvent
    fun register(event: FMLConstructModEvent) {
        if (ModList.get().isLoaded(MOD_ID)) {
            ReplicationConnectorUpgrades.addSlots(1)
            ReplicationConnectorExtensions
                .registerNodeService<IEnergyDistributor>(::ReplicationEnergyDistributor)
        }
    }

    @JvmStatic
    @SubscribeEvent
    fun onCommonSetup(event: FMLCommonSetupEvent) {
        if (!ModList.get().isLoaded(MOD_ID)) return
        event.enqueueWork {
            Upgrades.add(AFItemAndBlock.INDUCTION_CARD, AppRepBlocks.REPLICATION_CONNECTOR, 1)
        }
    }

    const val MOD_ID = "appflux"
}
