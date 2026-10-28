package dev.lapis256.apprep.client

import appeng.client.api.AEKeyRendering
import appeng.api.client.StorageCellModels
import appeng.client.InitScreens
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.ae2.stack.MatterKeyType
import dev.lapis256.apprep.client.gui.ReplicationConnectorScreen
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.common.init.AppRepMenus
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent


@Mod(value = AppliedReplicaticsAPI.MOD_ID, dist = [Dist.CLIENT])
class AppliedReplicaticsClient(modBus: IEventBus) {

    init {
        modBus.addListener(::onCommonSetup)
        modBus.addListener(::onClientSetup)
        modBus.addListener(::registerMenuScreens)
    }

    /**
     * @see appeng.core.AppEngBase.commonSetup
     * @see appeng.core.AppEngBase.postRegistrationInitialization
     * @see appeng.init.internal.InitStorageCells.init
     */
    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        event.enqueueWork {
            initModels()
        }
    }

    private fun initModels() {
        AppRepItems.CELLS.forEach { cell ->
            StorageCellModels.registerModel(
                cell,
                AppliedReplicaticsAPI.rl("block/drive/cells/${cell.tier.namePrefix()}_matter_cell")
            )
        }
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            AEKeyRendering.register(MatterKeyType, MatterKey::class.java, AE2MatterStackRenderer())
        }
    }

    private fun registerMenuScreens(event: RegisterMenuScreensEvent) {
        InitScreens.register(
            event,
            AppRepMenus.REPLICATION_CONNECTOR,
            ::ReplicationConnectorScreen,
            "/screens/apprep/replication_connector.json"
        )
    }
}
