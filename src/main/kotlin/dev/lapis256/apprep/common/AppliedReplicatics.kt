package dev.lapis256.apprep.common

import appeng.api.behaviors.ContainerItemStrategy
import appeng.api.behaviors.GenericSlotCapacities
import appeng.api.crafting.PatternDetailsHelper
import appeng.api.stacks.AEKeyTypes
import appeng.api.upgrades.Upgrades
import appeng.capabilities.Capabilities
import appeng.core.definitions.AEItems
import appeng.core.localization.GuiText
import com.buuz135.replication.ReplicationRegistry
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.ae2.stack.MatterKeyType
import dev.lapis256.apprep.api.connector.ReplicationConnectorExtensions
import dev.lapis256.apprep.api.connector.ReplicationConnectorUpgrades
import dev.lapis256.apprep.common.ae2.crafting.ReplicationPatternDecoder
import dev.lapis256.apprep.common.ae2.strategies.GenericStackMatterStorage
import dev.lapis256.apprep.common.ae2.strategies.MatterTankItemStrategy
import dev.lapis256.apprep.common.init.AppRepBlockEntities
import dev.lapis256.apprep.common.init.AppRepBlocks
import dev.lapis256.apprep.common.init.AppRepCreativeTab
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.common.init.AppRepMenus
import dev.lapis256.apprep.common.init.AppRepParts
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.RegisterEvent

@Mod(AppliedReplicaticsAPI.MOD_ID)
class AppliedReplicatics(
    context: FMLJavaModLoadingContext,
) {
    init {
        val eventBus = context.modEventBus
        AppRepParts.init()
        AppRepBlockEntities.REGISTRY.register(eventBus)
        AppRepBlocks.REGISTRY.register(eventBus)
        AppRepItems.REGISTRY.register(eventBus)
        AppRepCreativeTab.REGISTRY.register(eventBus)
        AppRepMenus.REPLICATION_CONNECTOR

        eventBus.addListener(::onCommonSetup)
        eventBus.addListener(::onRegister)
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity::class.java, ::attachMatterHandler)

        PatternDetailsHelper.registerDecoder(ReplicationPatternDecoder)

        @Suppress("UnstableApiUsage")
        run {
            ContainerItemStrategy.register(MatterKeyType, MatterKey::class.java, MatterTankItemStrategy)
            GenericSlotCapacities.register(MatterKeyType, 128L)
        }
    }

    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        event.enqueueWork {
            initUpgrades()
        }
    }

    private fun initUpgrades() {
        val storageCellGroup = GuiText.StorageCells.translationKey
        AppRepItems.CELLS.forEach {
            Upgrades.add(AEItems.INVERTER_CARD, it, 1, storageCellGroup)
            Upgrades.add(AEItems.EQUAL_DISTRIBUTION_CARD, it, 1, storageCellGroup)
            Upgrades.add(AEItems.VOID_CARD, it, 1, storageCellGroup)
        }

        ReplicationConnectorExtensions.freeze()
        ReplicationConnectorUpgrades.freeze()
    }

    private fun onRegister(event: RegisterEvent) {
        if (event.registryKey != Registries.BLOCK) {
            return
        }

        AEKeyTypes.register(MatterKeyType)
    }

    private fun attachMatterHandler(event: AttachCapabilitiesEvent<BlockEntity>) {
        val blockEntity = event.`object`
        event.addCapability(
            AppliedReplicaticsAPI.rl("matter_handler"),
            object : ICapabilityProvider {
                override fun <T : Any> getCapability(
                    cap: Capability<T>,
                    side: Direction?,
                ): LazyOptional<T> {
                    if (cap !== ReplicationRegistry.Capabilities.MATTER_HANDLER) return LazyOptional.empty()
                    return blockEntity
                        .getCapability(Capabilities.GENERIC_INTERNAL_INV, side)
                        .lazyMap(::GenericStackMatterStorage)
                        .cast()
                }
            },
        )
    }
}
