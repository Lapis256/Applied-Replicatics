package dev.lapis256.apprep.mixin_impl.replication

import com.buuz135.replication.api.IMatterType
import com.buuz135.replication.api.matter_fluid.MatterStack
import com.buuz135.replication.calculation.MatterCompound
import com.buuz135.replication.calculation.MatterValue
import com.llamalad7.mixinextras.sugar.ref.LocalRef
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import net.minecraft.core.BlockPos
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo


class MixinImplReplicationTask(private val extractMatter: (type: IMatterType, amount: Long) -> Long) {
    var internalMatterStacks: Object2LongMap<IMatterType> = Object2LongOpenHashMap()
        set(value) {
            field.clear()
            field.putAll(value)
        }

    /**
     * ME Replication Connector からの自動クラフト時に追加される内部タンクからマターを搬出するように処理を変更します
     * 足りなかったマターは本来の処理で搬出されます
     */
    fun extractMatterStacks(
        original: MatterCompound?,
        pos: BlockPos,
        oldOriginalRef: LocalRef<MatterCompound>,
        ci: CallbackInfo,
        storedMatterStack: HashMap<Long, List<MatterStack>>
    ): MatterCompound? {
        if (original == null) {
            return null
        }
        if (internalMatterStacks.isEmpty()) {
            return original
        }

        val extractedStacks = mutableListOf<MatterStack>()
        val remainingMatter = MatterCompound()

        var isExtractedFromInternal = false

        for (matterValue in original.values.values) {
            var amount = matterValue.amount.toLong()
            val extracted: Long = extractMatter(matterValue.matter, amount)

            if (extracted > 0) {
                isExtractedFromInternal = true
            }

            amount -= extracted
            if (amount > 0) {
                remainingMatter.add(MatterValue(matterValue.matter, amount.toDouble()))
                continue
            }

            extractedStacks.add(MatterStack(matterValue.matter, extracted.toDouble()))
        }

        if (!isExtractedFromInternal) {
            oldOriginalRef.set(null)
            return original
        }

        if (remainingMatter.cachedWeight > 0) {
            oldOriginalRef.set(original)
            return remainingMatter
        }

        oldOriginalRef.set(null)
        storedMatterStack[pos.asLong()] = extractedStacks
        ci.cancel()
        return original
    }

    /**
     * [extractMatterStacks] によって書き換えられた [MatterCompound] を元に戻します
     */
    fun restoreMatterAmount(originalAmount: Double, oldOriginal: MatterCompound?, type: IMatterType): Double {
        val originalMatter = oldOriginal ?: return originalAmount
        val value = originalMatter.values[type] ?: return originalAmount
        return value.amount
    }
}
