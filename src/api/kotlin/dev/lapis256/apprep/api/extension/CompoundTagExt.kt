package dev.lapis256.apprep.api.extension

import com.mojang.serialization.Codec
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps

fun <T> CompoundTag.putCodec(
    codec: Codec<T>,
    value: T,
) {
    codec
        .encodeStart(NbtOps.INSTANCE, value)
        .result()
        .ifPresent { tag ->
            this.merge(tag as CompoundTag)
        }
}

fun <T> CompoundTag.getCodec(codec: Codec<T>): T? = codec.parse(NbtOps.INSTANCE, this).result().orElse(null)
