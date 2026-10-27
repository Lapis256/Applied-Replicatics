package dev.lapis256.apprep.integration.jei.client

import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.replication.util.renderMatterTypeOnGui
import mezz.jei.api.ingredients.IIngredientRenderer
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.Item
import net.minecraft.world.entity.player.Player


class MatterStackRenderer : IIngredientRenderer<MatterStack> {
    override fun render(guiGraphics: GuiGraphicsExtractor, ingredient: MatterStack) {
        renderMatterTypeOnGui(guiGraphics, ingredient.matterType)
    }

    @Suppress("removal")
    @Deprecated("Removal in JEI 19.5.4")
    override fun getTooltip(ingredient: MatterStack, tooltipFlag: TooltipFlag): List<Component> =
        listOf(ingredient.displayName)

    override fun getTooltip(
        ingredient: MatterStack,
        tooltipContext: Item.TooltipContext,
        player: Player?,
        tooltipFlag: TooltipFlag
    ): List<Component> = listOf(ingredient.displayName)
}
