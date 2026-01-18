package dev.lapis256.apprep.client

import appeng.api.client.AEKeyRendering
import appeng.api.client.StorageCellModels
import appeng.items.storage.BasicStorageCell
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.ae2.stack.MatterKeyType
import dev.lapis256.apprep.common.init.AppRepItems
import net.minecraft.client.color.item.ItemColor
import net.minecraft.util.FastColor
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent


@Mod(value = AppliedReplicaticsAPI.MOD_ID, dist = [Dist.CLIENT])
class AppliedReplicaticsClient(modBus: IEventBus) {

    init {
        modBus.addListener(::onCommonSetup)
        modBus.addListener(::onClientSetup)
        modBus.addListener(::onRegisterItemColors)
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

    private fun makeOpaque(itemColor: ItemColor) = ItemColor { stack, tintIndex ->
        FastColor.ARGB32.opaque(itemColor.getColor(stack, tintIndex))
    }

    private fun onRegisterItemColors(event: RegisterColorHandlersEvent.Item) {
        event.register(makeOpaque(BasicStorageCell::getColor), *AppRepItems.CELLS.toTypedArray())
    }
}
