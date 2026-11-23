package dev.lapis256.apprep.common.logic

import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.api.stacks.KeyCounter
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.CompoundTag

// TODO(next major): Remove with the legacy ReplicationConnectorLogic pending-task migration when bumping the mod major version.
data class PendingTask(
    val input: KeyCounter,
    val output: AEItemKey,
    var count: Long,
) {
    constructor(inputHolder: Array<KeyCounter>, output: AEItemKey) : this(
        KeyCounter().apply { inputHolder.forEach(::addAll) },
        output,
        0L,
    )

    companion object {
        val INPUT_CODEC: Codec<KeyCounter> =
            RecordCodecBuilder
                .create<Pair<AEKey, Long>> {
                    it
                        .group(
                            CompoundTag.CODEC
                                .comapFlatMap({ tag ->
                                    AEKey.fromTagGeneric(tag)?.let(DataResult<AEKey>::success) ?: DataResult.error { "Invalid AE key" }
                                }, AEKey::toTagGeneric)
                                .fieldOf("key")
                                .forGetter { entry -> entry.first },
                            Codec.LONG.fieldOf("value").forGetter { entry -> entry.second },
                        ).apply(it, ::Pair)
                }.listOf()
                .xmap(
                    { list ->
                        KeyCounter().apply {
                            list.forEach { (key, value) ->
                                add(key, value)
                            }
                        }
                    },
                    { counter ->
                        counter.map {
                            Pair(it.key, it.longValue)
                        }
                    },
                )

        val CODEC: Codec<PendingTask> =
            RecordCodecBuilder.create {
                it
                    .group(
                        INPUT_CODEC.fieldOf("input").forGetter(PendingTask::input),
                        CompoundTag.CODEC
                            .comapFlatMap({ tag ->
                                AEItemKey.fromTag(tag)?.let(DataResult<AEItemKey>::success) ?: DataResult.error { "Invalid AE item key" }
                            }, AEItemKey::toTag)
                            .fieldOf("output")
                            .forGetter(PendingTask::output),
                        Codec.LONG.fieldOf("count").forGetter(PendingTask::count),
                    ).apply(it, ::PendingTask)
            }
    }

    fun increaseProcessingCount() {
        count++
    }
}
