package dev.apprep.apprep.integration.jei.client

import com.buuz135.replication.ReplicationRegistry
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.ingredients.IIngredientType
import mezz.jei.api.registration.IModIngredientRegistration
import net.neoforged.fml.ModList
import tamaized.ae2jeiintegration.api.integrations.jei.IngredientConverters
import kotlin.streams.asSequence


@JeiPlugin
class AppRepJEI : IModPlugin {
    init {
        if (ModList.get().isLoaded("ae2jeiintegration")) {
            IngredientConverters.register(MatterIngredientConverter())
        }
    }

    companion object {
        val TYPE_MATTER = IIngredientType { MatterStack::class.java }
    }

    override fun getPluginUid() = AppliedReplicaticsAPI.rl("jei_plugin")

    override fun registerIngredients(registry: IModIngredientRegistration) {
        val types = ReplicationRegistry.MATTER_TYPES_REGISTRY.holders()
            .asSequence()
            .filterNot(ReplicationRegistry.Matter.EMPTY::equals)
            .map { MatterStack(it.value(), 1.0) }
            .toList()

        registry.register(
            TYPE_MATTER,
            types,
            MatterStackHelper,
            MatterStackRenderer(),
            ReplicationRegistry.MATTER_TYPES_REGISTRY.byNameCodec().xmap({ MatterStack(it, 1.0) }, MatterStack::getMatterType),
        )
    }
}
