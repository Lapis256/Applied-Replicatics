package dev.lapis256.apprep.integration.emi.client

import com.buuz135.replication.ReplicationRegistry
import com.buuz135.replication.api.MatterType
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.serializer.EmiStackSerializer
import dev.lapis256.apprep.api.replication.util.MATTER_TYPES
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.resources.ResourceLocation


object MatterEmiStackSerializer : EmiStackSerializer<MatterEmiStack> {
    override fun create(id: ResourceLocation, componentChanges: DataComponentPatch, amount: Long): EmiStack {
        val type = ReplicationRegistry.MATTER_TYPES_REGISTRY.get(id)
            .takeIf { it != MatterType.EMPTY }
            ?: return EmiStack.EMPTY

        return MatterEmiStack(type, amount)
    }

    override fun getType() = "apprep_matter"

    fun addEmiStacks(registry: EmiRegistry) {
        for (type in MATTER_TYPES) {
            registry.addEmiStack(MatterEmiStack(type, 1))
        }
    }
}
