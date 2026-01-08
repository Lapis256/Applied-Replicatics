package dev.lapis256.apprep.api.util

import com.buuz135.replication.Replication
import com.buuz135.replication.api.IMatterType
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4


fun renderMatterTypeOnGui(guiGraphics: GuiGraphics, type: IMatterType, x: Int = 0, y: Int = 0) {
    val (r, g, b, a) = type.color.get()
    val icon = ResourceLocation.fromNamespaceAndPath(Replication.MOD_ID, "textures/gui/mattertypes/${type.name.lowercase()}.png")

    guiGraphics.setColor(r, g, b, a)
    RenderSystem.enableBlend()
    guiGraphics.blit(icon, x, y, 2, 0F, 0F, 16, 16, 16, 16)
    RenderSystem.disableBlend()
    guiGraphics.setColor(1F, 1F, 1F, 1F)
}
