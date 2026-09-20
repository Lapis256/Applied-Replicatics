package dev.lapis256.apprep.api.replication.task

import appeng.api.stacks.AEItemKey
import com.buuz135.replication.api.IMatterType
import com.buuz135.replication.api.task.IReplicationTask
import com.buuz135.replication.api.task.ReplicationTask
import com.mojang.serialization.Codec
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.replication.util.MATTER_TYPE_NAME_CODEC
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import kotlin.math.max


interface MEReplicationTask {
    fun `apprep$setInternalMatterStacks`(stacks: Object2LongMap<IMatterType>)
    fun `apprep$getInternalMatterStacks`(): Object2LongMap<IMatterType>
    fun `apprep$setAutoCraftingTask`(autoCraftingTask: Boolean)
    fun `apprep$isAutoCraftingTask`(): Boolean

    /**
     * 自動クラフトの材料として搬出されたマター
     */
    private var internalMatterStacks: Object2LongMap<IMatterType>
        get() = `apprep$getInternalMatterStacks`()
        set(value) = `apprep$setInternalMatterStacks`(value)

    private var autoCraftingTask: Boolean
        get() = `apprep$isAutoCraftingTask`()
        set(value) = `apprep$setAutoCraftingTask`(value)

    fun deserializeAdditionalNBT(provider: HolderLookup.Provider, compoundTag: CompoundTag) {
        val ops = provider.createSerializationContext(NbtOps.INSTANCE)
        INTERNAL_MATTER_STACKS_CODEC.parse(ops, compoundTag)
            .ifSuccess {
                internalMatterStacks = it
            }

        autoCraftingTask = compoundTag.getBoolean(AUTO_CRAFTING_TASK_TAG)
    }

    fun serializeAdditionalNBT(provider: HolderLookup.Provider, compoundTag: CompoundTag): CompoundTag {
        val ops = provider.createSerializationContext(NbtOps.INSTANCE)

        INTERNAL_MATTER_STACKS_CODEC.encodeStart(ops, internalMatterStacks)
            .ifSuccess {
                compoundTag.merge(it as CompoundTag)
            }

        if (autoCraftingTask) {
            compoundTag.putBoolean(AUTO_CRAFTING_TASK_TAG, true)
        }

        return compoundTag
    }

    fun extractMatter(type: IMatterType, amount: Long): Long {
        val maxAmount = internalMatterStacks.getLong(type)
        val toExtract = minOf(amount, maxAmount)
        if (toExtract > 0) {
            internalMatterStacks.put(type, maxAmount - toExtract)
        }
        return toExtract
    }

    companion object {
        private const val AUTO_CRAFTING_TASK_TAG = "${AppliedReplicaticsAPI.MOD_ID}:auto_crafting_task"

        val MATTER_COUNT_CODEC: Codec<Object2LongMap<IMatterType>> = Codec.unboundedMap(
            MATTER_TYPE_NAME_CODEC,
            Codec.LONG
        ).xmap(::Object2LongOpenHashMap, Map<IMatterType, Long>::toMap)

        val INTERNAL_MATTER_STACKS_CODEC: Codec<Object2LongMap<IMatterType>> =
            MATTER_COUNT_CODEC.fieldOf("${AppliedReplicaticsAPI.MOD_ID}:internal_matter_stacks").codec()

        fun create(
            internalMatterStacks: Object2LongMap<IMatterType>,
            output: AEItemKey,
            totalAmount: Long,
            source: BlockPos,
            mode: IReplicationTask.Mode = IReplicationTask.Mode.MULTIPLE,
            autoCraftingTask: Boolean = false
        ): ReplicationTask {
            return ReplicationTask(
                output.toStack(),
                max(1, totalAmount.toInt()),
                mode,
                source,
                false
            ).apply {
                this.internalMatterStacks = internalMatterStacks
                this.autoCraftingTask = autoCraftingTask
            }
        }
    }
}
