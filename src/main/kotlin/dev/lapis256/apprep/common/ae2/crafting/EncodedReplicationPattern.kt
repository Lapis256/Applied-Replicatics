package dev.lapis256.apprep.common.ae2.crafting

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack


data class EncodedReplicationPattern(val output: ItemStack) {
    companion object {
        val CODEC: Codec<EncodedReplicationPattern> = RecordCodecBuilder.create {
            it.group(
                ItemStack.CODEC.fieldOf("output").forGetter(EncodedReplicationPattern::output)
            ).apply(it, ::EncodedReplicationPattern)
        }
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, EncodedReplicationPattern> =
            StreamCodec.composite(
                ItemStack.STREAM_CODEC, EncodedReplicationPattern::output,
                ::EncodedReplicationPattern
            )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return ItemStack.matches(output, (other as EncodedReplicationPattern).output)
    }

    override fun hashCode(): Int {
        return ItemStack.hashItemAndComponents(output)
    }
}
