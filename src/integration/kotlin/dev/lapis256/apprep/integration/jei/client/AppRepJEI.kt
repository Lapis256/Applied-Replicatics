package dev.lapis256.apprep.integration.jei.client

import appeng.api.integrations.jei.IngredientConverters
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.replication.util.MATTER_TYPES
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.ingredients.IIngredientType
import mezz.jei.api.registration.IModIngredientRegistration

@JeiPlugin
class AppRepJEI : IModPlugin {
    companion object {
        val TYPE_MATTER = IIngredientType { MatterStack::class.java }
    }

    override fun getPluginUid() = AppliedReplicaticsAPI.rl("jei_plugin")

    override fun registerIngredients(registry: IModIngredientRegistration) {
        IngredientConverters.register(MatterIngredientConverter())
        val types = MATTER_TYPES.map { MatterStack(it, 1) }
        registry.register(
            TYPE_MATTER,
            types,
            MatterStackHelper,
            MatterStackRenderer(),
        )
    }
}
