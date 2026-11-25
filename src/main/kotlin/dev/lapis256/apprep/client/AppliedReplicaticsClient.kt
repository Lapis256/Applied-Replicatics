package dev.lapis256.apprep.client

import appeng.api.client.AEKeyRendering
import appeng.api.client.StorageCellModels
import appeng.init.client.InitScreens
import appeng.items.storage.BasicStorageCell
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.ae2.stack.MatterKeyType
import dev.lapis256.apprep.client.gui.ReplicationConnectorScreen
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.common.init.AppRepMenus
import net.minecraft.client.color.item.ItemColor
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.RegisterColorHandlersEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent

@Mod.EventBusSubscriber(modid = AppliedReplicaticsAPI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
object AppliedReplicaticsClient {
    /**
     * @see appeng.core.AppEngBase.commonSetup
     * @see appeng.core.AppEngBase.postRegistrationInitialization
     * @see appeng.init.internal.InitStorageCells.init
     */
    @JvmStatic
    @SubscribeEvent
    fun onCommonSetup(event: FMLCommonSetupEvent) {
        event.enqueueWork {
            initModels()
        }
    }

    private fun initModels() {
        AppRepItems.CELLS.forEach { cell ->
            StorageCellModels.registerModel(
                cell,
                AppliedReplicaticsAPI.rl("block/drive/cells/${cell.tier.namePrefix()}_matter_cell"),
            )
        }
    }

    @JvmStatic
    @SubscribeEvent
    fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            AEKeyRendering.register(MatterKeyType, MatterKey::class.java, AE2MatterStackRenderer())
            registerMenuScreens()
        }
    }

    private fun makeOpaque(itemColor: ItemColor) =
        ItemColor { stack, tintIndex ->
            itemColor.getColor(stack, tintIndex) or 0xFF000000.toInt()
        }

    @JvmStatic
    @SubscribeEvent
    fun onRegisterItemColors(event: RegisterColorHandlersEvent.Item) {
        event.register(makeOpaque(BasicStorageCell::getColor), *AppRepItems.CELLS.toTypedArray())
    }

    private fun registerMenuScreens() {
        InitScreens.register(
            AppRepMenus.REPLICATION_CONNECTOR,
            ::ReplicationConnectorScreen,
            "/screens/apprep/replication_connector.json",
        )
    }
}
