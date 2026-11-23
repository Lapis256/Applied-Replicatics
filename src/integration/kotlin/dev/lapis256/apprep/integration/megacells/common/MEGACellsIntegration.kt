package dev.lapis256.apprep.integration.megacells.common

import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGABlocks
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGAItems
import net.minecraft.core.registries.Registries
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent
import net.minecraftforge.fml.javafmlmod.FMLModContainer
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.RegisterEvent

@Mod.EventBusSubscriber(modid = AppliedReplicaticsAPI.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
object MEGACellsIntegration {
    const val MOD_ID = "megacells"
    internal val ITEM_REGISTRY = DeferredRegister.create(Registries.ITEM, AppliedReplicaticsAPI.MOD_ID)
    val isLoaded: Boolean by lazy { ModList.get().isLoaded(MOD_ID) }

    @JvmStatic
    @SubscribeEvent
    fun register(event: FMLConstructModEvent) {
        if (isLoaded) {
            val container = ModList.get().getModContainerById(AppliedReplicaticsAPI.MOD_ID).orElseThrow() as FMLModContainer
            val modBus = container.eventBus
            AppRepMEGABlocks.REGISTRY.register(modBus)
            ITEM_REGISTRY.register(modBus)
        }
    }

    @JvmStatic
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun prepareItems(event: RegisterEvent) {
        if (isLoaded && event.registryKey == Registries.ITEM) {
            AppRepMEGAItems.prepareRegistration()
        }
    }
}
