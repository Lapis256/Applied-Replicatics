package dev.lapis256.apprep.integration.jei.client

import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.replication.util.MATTER_TYPES
import dev.lapis256.apprep.api.replication.util.MATTER_TYPE_NAME_CODEC
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.ingredients.IIngredientType
import mezz.jei.api.registration.IModIngredientRegistration
import net.neoforged.fml.ModList
import tamaized.ae2jeiintegration.api.integrations.jei.IngredientConverters


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
        val types = MATTER_TYPES.map { MatterStack(it, 1.0) }
        registry.register(
            TYPE_MATTER,
            types,
            MatterStackHelper,
            MatterStackRenderer(),
            MATTER_TYPE_NAME_CODEC.xmap({ MatterStack(it, 1.0) }, MatterStack::getMatterType),
        )
    }
}
