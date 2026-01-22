package dev.lapis256.apprep.integration.emi.client

import appeng.api.integrations.emi.EmiStackConverters
import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiInitRegistry
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry


@EmiEntrypoint
class AppRepEMI : EmiPlugin {
    override fun initialize(registry: EmiInitRegistry) {
        registry.addIngredientSerializer(MatterEmiStack::class.java, MatterEmiStackSerializer)
    }

    override fun register(registry: EmiRegistry) {
        MatterEmiStackSerializer.addEmiStacks(registry)

        EmiStackConverters.register(MatterStackConverter)
    }
}
