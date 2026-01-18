package dev.lapis256.apprep.common

import appeng.api.AECapabilities
import appeng.api.behaviors.ContainerItemStrategy
import appeng.api.behaviors.GenericSlotCapacities
import appeng.api.crafting.PatternDetailsHelper
import appeng.api.networking.IInWorldGridNodeHost
import appeng.api.stacks.AEKeyTypes
import appeng.api.upgrades.Upgrades
import appeng.core.definitions.AEItems
import appeng.core.definitions.DeferredBlockEntityType
import appeng.core.localization.GuiText
import appeng.parts.automation.StackWorldBehaviors
import com.buuz135.replication.ReplicationRegistry
import com.buuz135.replication.block.MatterPipeBlock
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.ae2.stack.MatterKeyType
import dev.lapis256.apprep.common.ae2.crafting.ReplicationPatternDecoder
import dev.lapis256.apprep.common.ae2.strategies.GenericStackMatterStorage
import dev.lapis256.apprep.common.ae2.strategies.MatterHandlerExternalStorageStrategy
import dev.lapis256.apprep.common.ae2.strategies.MatterTankItemStrategy
import dev.lapis256.apprep.common.block.ReplicationConnectorBlock
import dev.lapis256.apprep.common.init.*
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.registries.RegisterEvent


@Mod(AppliedReplicaticsAPI.MOD_ID)
class AppliedReplicatics(eventBus: IEventBus) {

    init {
        AppRepParts.init()
        AppRepBlockEntities.REGISTRY.register(eventBus)
        AppRepBlocks.REGISTRY.register(eventBus)
        AppRepComponents.REGISTRY.register(eventBus)
        AppRepItems.REGISTRY.register(eventBus)
        AppRepCreativeTab.REGISTRY.register(eventBus)

        MatterPipeBlock.ALLOWED_CONNECTION_BLOCKS.add { it is ReplicationConnectorBlock }

        eventBus.addListener(::onCommonSetup)
        eventBus.addListener(::onRegister)
        eventBus.addListener(::registerCapabilities)
        eventBus.addListener(::registerGenericAdapters)

        PatternDetailsHelper.registerDecoder(ReplicationPatternDecoder)

        @Suppress("UnstableApiUsage")
        run {
            StackWorldBehaviors.registerExternalStorageStrategy(MatterKeyType, ::MatterHandlerExternalStorageStrategy)

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
    }

    private fun onRegister(event: RegisterEvent) {
        if (event.registryKey != Registries.BLOCK) {
            return
        }

        AEKeyTypes.register(MatterKeyType)
    }

    private fun registerCapabilities(event: RegisterCapabilitiesEvent) {
        AppRepBlockEntities.BLOCK_ENTITY_TYPES
            .asSequence()
            .filter { IInWorldGridNodeHost::class.java.isAssignableFrom(it.blockEntityClass) }
            .map(DeferredBlockEntityType<*>::get)
            .forEach { type ->
                event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, type) { blockEntity, _ ->
                    blockEntity as IInWorldGridNodeHost
                }
            }
    }

    @Suppress("UnstableApiUsage")
    private fun registerGenericAdapters(event: RegisterCapabilitiesEvent) {
        for (block in BuiltInRegistries.BLOCK) {
            if (event.isBlockRegistered(AECapabilities.GENERIC_INTERNAL_INV, block)) {
                event.registerBlock(ReplicationRegistry.Capabilities.MATTER_HANDLER, { level, pos, state, blockEntity, context ->
                    val inv = level.getCapability(AECapabilities.GENERIC_INTERNAL_INV, pos, state, blockEntity, context) ?: return@registerBlock null
                    GenericStackMatterStorage(inv)
                }, block)
            }
        }
    }
}
