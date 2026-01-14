package dev.lapis256.apprep.common.init

import com.mojang.serialization.Codec
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.ae2.crafting.EncodedReplicationPattern
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.neoforged.neoforge.registries.DeferredRegister


object AppRepComponents {
    val REGISTRY: DeferredRegister<DataComponentType<*>> = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AppliedReplicaticsAPI.MOD_ID)

    val ENCODED_REPLICATION_PATTERN = register(
        "encoded_replication_pattern",
        EncodedReplicationPattern.CODEC,
        EncodedReplicationPattern.STREAM_CODEC
    )

    fun <T> register(name: String, codec: Codec<T>, streamCodec: StreamCodec<in RegistryFriendlyByteBuf, T>): DataComponentType<T> {
        return DataComponentType.builder<T>()
            .persistent(codec)
            .networkSynchronized(streamCodec)
            .build().also {
                REGISTRY.register(name) { -> it }
            }
    }
}
