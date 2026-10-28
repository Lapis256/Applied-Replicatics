package dev.lapis256.apprep.api.replication.util

import com.buuz135.replication.Replication
import com.buuz135.replication.api.IMatterType
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import net.minecraft.util.ARGB
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4


fun renderMatterTypeOnGui(guiGraphics: GuiGraphicsExtractor, type: IMatterType, x: Int = 0, y: Int = 0) {
    val (r, g, b, a) = type.color.get()
    val icon = Identifier.fromNamespaceAndPath(Replication.MOD_ID, "textures/gui/mattertypes/${type.name.lowercase()}.png")

    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon, x, y, 0F, 0F, 16, 16, 16, 16, ARGB.colorFromFloat(a, r, g, b))
}
