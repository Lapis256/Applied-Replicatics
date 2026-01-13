package dev.lapis256.apprep.client

import appeng.api.client.AEKeyRendering
import appeng.items.storage.BasicStorageCell
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.ae2.stack.MatterKeyType
import dev.lapis256.apprep.common.init.AppRepItems
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent


@Mod(value = AppliedReplicaticsAPI.MOD_ID, dist = [Dist.CLIENT])
class AppliedReplicaticsClient(modBus: IEventBus) {

    init {
        modBus.addListener(::onClientSetup)
        modBus.addListener(::onRegisterItemColors)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            AEKeyRendering.register(MatterKeyType, MatterKey::class.java, AE2MatterStackRenderer())
        }
    }

    private fun onRegisterItemColors(event: RegisterColorHandlersEvent.Item) {
        event.register(BasicStorageCell::getColor, *AppRepItems.CELLS.toTypedArray())
    }
}
