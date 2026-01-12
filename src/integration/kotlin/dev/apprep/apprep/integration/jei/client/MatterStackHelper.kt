package dev.apprep.apprep.integration.jei.client

import com.buuz135.replication.ReplicationAttachments
import com.buuz135.replication.ReplicationConfig
import com.buuz135.replication.ReplicationRegistry
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.replication.util.getMatterId
import mezz.jei.api.ingredients.IIngredientHelper
import mezz.jei.api.ingredients.subtypes.UidContext
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import kotlin.math.roundToInt


object MatterStackHelper : IIngredientHelper<MatterStack> {
    override fun getIngredientType() = AppRepJEI.TYPE_MATTER

    override fun getDisplayName(ingredient: MatterStack): String =
        ingredient.displayName.string

    override fun copyIngredient(ingredient: MatterStack): MatterStack =
        ingredient.copy()

    override fun normalizeIngredient(ingredient: MatterStack) =
        MatterStack(ingredient, 1.0)

    override fun isValidIngredient(ingredient: MatterStack) =
        !ingredient.isEmpty

    override fun getUid(ingredient: MatterStack, context: UidContext): Any =
        ingredient.matterType

    @Suppress("removal")
    @Deprecated("Removal in JEI 19.9.0")
    override fun getUniqueId(ingredient: MatterStack, context: UidContext) =
        "matter:${ingredient.matterType.name}"

    override fun getResourceLocation(ingredient: MatterStack): ResourceLocation =
        getMatterId(ingredient.matterType)

    override fun getColors(ingredient: MatterStack): Iterable<Int> =
        ingredient.matterType.color.get().map { i -> (i * 255).roundToInt() }

    override fun getCheatItemStack(ingredient: MatterStack): ItemStack {
        if (ingredient.isEmpty) {
            return ItemStack.EMPTY
        }

        val tileData = CompoundTag().apply {
            val matterStack = MatterStack(ingredient, ReplicationConfig.MatterTank.CAPACITY.toDouble())
            val tankData = matterStack.writeToNBT(CompoundTag())
            put("tank", tankData)
        }

        return ItemStack(ReplicationRegistry.Blocks.CREATIVE_MATTER_TANK).apply {
            set(ReplicationAttachments.TILE, tileData)
        }
    }

    override fun getErrorInfo(ingredient: MatterStack?): String {
        val stack = ingredient ?: return "null"
        return "MatterStack{type=${stack.matterType.name}, amount=${stack.amount}}"
    }
}
