package dev.lapis256.apprep.api.ae2.stack

import appeng.api.stacks.AEKey
import com.buuz135.replication.api.IMatterType
import com.buuz135.replication.api.matter_fluid.MatterStack
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.lapis256.apprep.api.replication.util.MATTER_TYPE_NAME_CODEC
import dev.lapis256.apprep.api.replication.util.getMatterId
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level


class MatterKey private constructor(val stack: MatterStack) : AEKey() {
    private constructor(type: IMatterType) : this(MatterStack(type, 1.0))

    companion object {
        val MAP_CODEC: MapCodec<MatterKey> = RecordCodecBuilder.mapCodec { builder ->
            builder.group(
                MATTER_TYPE_NAME_CODEC.fieldOf("type").forGetter(MatterKey::type)
            ).apply(builder, ::MatterKey)
        }

        val CODEC: Codec<MatterKey> = MAP_CODEC.codec()

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, MatterKey> = ByteBufCodecs.fromCodecWithRegistries(CODEC)

        fun fromPacket(data: RegistryFriendlyByteBuf): MatterKey {
            return STREAM_CODEC.decode(data)
        }

        fun of(type: IMatterType): MatterKey {
            return MatterKey(type)
        }

        fun of(stack: MatterStack): MatterKey? {
            if (stack.isEmpty) {
                return null
            }

            return MatterKey(stack.matterType)
        }
    }

    val type: IMatterType = stack.matterType

    fun toStack(amount: Long): MatterStack {
        return MatterStack(stack.matterType, amount.toDouble())
    }

    override fun toTag(registries: HolderLookup.Provider): CompoundTag {
        val ops = registries.createSerializationContext(NbtOps.INSTANCE)
        return CODEC.encodeStart(ops, this).getOrThrow() as? CompoundTag ?: error("Failed to serialize MatterKey to NBT")
    }

    override fun writeToPacket(data: RegistryFriendlyByteBuf) {
        STREAM_CODEC.encode(data, this)
    }

    override fun addDrops(amount: Long, drops: List<ItemStack>?, level: Level?, pos: BlockPos?) {
        // No-op
    }

    val location: ResourceLocation by lazy { getMatterId(stack.matterType) }
    override fun getId(): ResourceLocation = location

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val key = other as MatterKey
        return key.stack.isMatterEqual(stack)
    }

    private val hashCode: Int by lazy { stack.matterType.hashCode() }
    override fun hashCode(): Int = hashCode

    override fun getType() = MatterKeyType
    override fun getPrimaryKey(): Any = stack.matterType
    override fun computeDisplayName(): Component = stack.displayName
    override fun dropSecondary(): AEKey = this
    override fun hasComponents() = false
}
