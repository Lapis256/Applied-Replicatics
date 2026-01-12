package dev.lapis256.apprep.api.replication.util

import com.buuz135.replication.ReplicationRegistry
import com.buuz135.replication.api.IMatterType
import com.buuz135.replication.api.MatterType
import com.mojang.serialization.Codec
import net.minecraft.resources.ResourceLocation


/**
 * MatterType.EMPTY を除く登録されているマタータイプのリスト
 */
val MATTER_TYPES by lazy {
    ReplicationRegistry.MATTER_TYPES_REGISTRY.filter { it != MatterType.EMPTY }
}

val MATTER_TYPE_NAME_CODEC: Codec<IMatterType> = ReplicationRegistry.MATTER_TYPES_REGISTRY.byNameCodec()

/**
 * 指定されたマタータイプの [ResourceLocation] を取得する。
 *
 * @throws IllegalArgumentException 未知のマタータイプが指定された場合
 */
fun getMatterId(type: IMatterType): ResourceLocation {
    return ReplicationRegistry.MATTER_TYPES_REGISTRY.getKey(type) ?: error("Unknown MatterType: $type")
}
