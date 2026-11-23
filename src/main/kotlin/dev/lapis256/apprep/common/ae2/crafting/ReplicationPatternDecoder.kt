package dev.lapis256.apprep.common.ae2.crafting

import appeng.api.crafting.IPatternDetails
import appeng.api.crafting.IPatternDetailsDecoder
import appeng.api.stacks.AEItemKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

object ReplicationPatternDecoder : IPatternDetailsDecoder {
    const val PATTERN_TAG = "apprep:encoded_replication_pattern"

    override fun isEncodedPattern(stack: ItemStack): Boolean = stack.getTagElement(PATTERN_TAG) != null

    override fun decodePattern(
        what: AEItemKey?,
        level: Level?,
    ): IPatternDetails? {
        if (what == null) {
            return null
        }
        val stack = what.toStack()
        return decodePattern(stack, level, false)
    }

    override fun decodePattern(
        stack: ItemStack,
        level: Level?,
        tryRecovery: Boolean,
    ): IPatternDetails? {
        val encoded = stack.getTagElement(PATTERN_TAG) ?: return null
        val output = ItemStack.of(encoded)
        return if (output.isEmpty) null else ReplicationPattern(output, stack.copy())
    }
}
