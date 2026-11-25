package dev.lapis256.apprep.integration.jei.client

import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.replication.util.renderMatterTypeOnGui
import mezz.jei.api.ingredients.IIngredientRenderer
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.item.TooltipFlag

class MatterStackRenderer : IIngredientRenderer<MatterStack> {
    @Deprecated("Required by JEI 15 for compatibility")
    override fun render(
        guiGraphics: GuiGraphics,
        ingredient: MatterStack,
    ) {
        renderMatterTypeOnGui(guiGraphics, ingredient.matterType)
    }

    @Deprecated("Required by JEI 15 for compatibility")
    override fun getTooltip(
        ingredient: MatterStack,
        tooltipFlag: TooltipFlag,
    ): List<Component> = listOf(ingredient.displayName)
}
