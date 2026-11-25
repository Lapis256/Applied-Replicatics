package dev.lapis256.apprep.api.ae2.stack

import appeng.api.stacks.AEKey
import com.buuz135.replication.api.IMatterType
import com.buuz135.replication.api.matter_fluid.MatterStack
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.lapis256.apprep.api.replication.util.MATTER_TYPE_NAME_CODEC
import dev.lapis256.apprep.api.replication.util.getMatterId
import io.netty.handler.codec.DecoderException
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class MatterKey private constructor(
    val stack: MatterStack,
) : AEKey() {
    private constructor(type: IMatterType) : this(MatterStack(type, 1))

    companion object {
        val MAP_CODEC: MapCodec<MatterKey> by lazy {
            RecordCodecBuilder.mapCodec { builder ->
                builder
                    .group(
                        MATTER_TYPE_NAME_CODEC.fieldOf("type").forGetter(MatterKey::type),
                    ).apply(builder, ::MatterKey)
            }
        }

        val CODEC: Codec<MatterKey> by lazy { MAP_CODEC.codec() }

        fun fromPacket(data: FriendlyByteBuf): MatterKey {
            val tag = data.readNbt() ?: throw DecoderException("Missing MatterKey NBT")
            return CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false) { throw DecoderException(it) }
        }

        fun of(type: IMatterType): MatterKey = MatterKey(type)

        fun of(stack: MatterStack): MatterKey? {
            if (stack.isEmpty) {
                return null
            }

            return MatterKey(stack.matterType)
        }
    }

    val type: IMatterType = stack.matterType

    fun toStack(amount: Long): MatterStack = MatterStack(stack.matterType, amount.coerceIn(0, Int.MAX_VALUE.toLong()).toInt())

    override fun toTag(): CompoundTag =
        CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElseThrow() as? CompoundTag ?: error("Failed to serialize MatterKey to NBT")

    override fun writeToPacket(data: FriendlyByteBuf) {
        data.writeNbt(toTag())
    }

    override fun addDrops(
        amount: Long,
        drops: List<ItemStack>?,
        level: Level?,
        pos: BlockPos?,
    ) {
        // No-op
    }

    val location: ResourceLocation by lazy { getMatterId(stack.matterType) }

    override fun getId(): ResourceLocation = location

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        val key = other as MatterKey
        return key.stack.isMatterEqual(stack)
    }

    private val hashCode: Int by lazy { stack.matterType.hashCode() }

    override fun hashCode(): Int = hashCode

    override fun getType() = MatterKeyType

    override fun getPrimaryKey(): Any = stack.matterType

    override fun computeDisplayName(): Component = stack.displayName

    override fun dropSecondary(): AEKey = this
}
