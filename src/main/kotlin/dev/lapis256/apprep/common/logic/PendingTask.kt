package dev.lapis256.apprep.common.logic

import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKey
import appeng.api.stacks.KeyCounter
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder


data class PendingTask(
    val input: KeyCounter,
    val output: AEItemKey,
    var count: Long
) {
    constructor(inputHolder: Array<KeyCounter>, output: AEItemKey) : this(
        KeyCounter().apply { inputHolder.forEach(::addAll) },
        output,
        0L
    )

    companion object {
        val INPUT_CODEC: Codec<KeyCounter> = RecordCodecBuilder.create<Pair<AEKey, Long>> {
            it.group(
                AEKey.CODEC.fieldOf("key").forGetter { entry -> entry.first },
                Codec.LONG.fieldOf("value").forGetter { entry -> entry.second }
            ).apply(it, ::Pair)
        }.listOf().xmap(
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
            }
        )

        val CODEC: Codec<PendingTask> = RecordCodecBuilder.create {
            it.group(
                INPUT_CODEC.fieldOf("input").forGetter(PendingTask::input),
                AEItemKey.CODEC.fieldOf("output").forGetter(PendingTask::output),
                Codec.LONG.fieldOf("count").forGetter(PendingTask::count)
            ).apply(it, ::PendingTask)
        }
    }

    fun increaseProcessingCount() {
        count++
    }
}
