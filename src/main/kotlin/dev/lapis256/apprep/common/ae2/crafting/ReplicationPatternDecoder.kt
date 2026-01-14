package dev.lapis256.apprep.common.ae2.crafting

import appeng.api.crafting.IPatternDetails
import appeng.api.crafting.IPatternDetailsDecoder
import appeng.api.stacks.AEItemKey
import dev.lapis256.apprep.common.init.AppRepComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level


object ReplicationPatternDecoder  : IPatternDetailsDecoder {
    override fun isEncodedPattern(stack: ItemStack): Boolean {
        return stack.has(AppRepComponents.ENCODED_REPLICATION_PATTERN)
    }

    override fun decodePattern(what: AEItemKey?, level: Level?): IPatternDetails? {
        if (what == null) {
            return null
        }
        val stack = what.toStack()
        val encoded = stack.get(AppRepComponents.ENCODED_REPLICATION_PATTERN) ?: return null
        return ReplicationPattern(encoded.output, stack.copy())
    }
}
