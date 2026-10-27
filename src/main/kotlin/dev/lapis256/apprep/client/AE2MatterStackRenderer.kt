package dev.lapis256.apprep.client

import appeng.client.api.AEKeyRenderer
import com.buuz135.replication.Replication
import com.mojang.blaze3d.vertex.PoseStack
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.replication.util.renderMatterTypeOnGui
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.level.Level


class AE2MatterStackRenderer : AEKeyRenderer<MatterKey, AE2MatterStackRenderer.State> {
    class State {
        lateinit var icon: Identifier
        var r = 1f
        var g = 1f
        var b = 1f
        var a = 1f
    }

    override fun getTooltip(stack: MatterKey): List<Component> = listOf(stack.displayName)

    override fun drawInGui(minecraft: Minecraft, guiGraphics: GuiGraphicsExtractor, x: Int, y: Int, stack: MatterKey) {
        renderMatterTypeOnGui(guiGraphics, stack.stack.matterType, x, y)
    }

    override fun stateClass(): Class<State> = State::class.java

    override fun createState() = State()

    override fun extract(state: State, stack: MatterKey, level: Level?, seed: Int) {
        val type = stack.stack.matterType
        val (r, g, b, a) = type.color.get()
        state.icon = Identifier.fromNamespaceAndPath(Replication.MOD_ID, "textures/gui/mattertypes/${type.name.lowercase()}.png")
        state.r = r
        state.g = g
        state.b = b
        state.a = a
    }

    override fun submit(poseStack: PoseStack, state: State, nodes: SubmitNodeCollector, combinedLight: Int) {
        poseStack.pushPose()
        poseStack.translate(0f, 0f, 0.001f)

        val x0 = -0.5f
        val y0 = 0.5f
        val x1 = 0.5f
        val y1 = -0.5f
        val (r, g, b, a) = listOf(state.r, state.g, state.b, state.a)
        nodes.submitCustomGeometry(poseStack, RenderTypes.text(state.icon)) { transform, buffer ->
        buffer.addVertex(transform, x0, y1, 0f)
            .setColor(r, g, b, a)
            .setUv(0F, 1F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(combinedLight)
            .setNormal(0f, 0f, 1f)
        buffer.addVertex(transform, x1, y1, 0f)
            .setColor(r, g, b, a)
            .setUv(1F, 1F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(combinedLight)
            .setNormal(0f, 0f, 1f)
        buffer.addVertex(transform, x1, y0, 0f)
            .setColor(r, g, b, a)
            .setUv(1F, 0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(combinedLight)
            .setNormal(0f, 0f, 1f)
        buffer.addVertex(transform, x0, y0, 0f)
            .setColor(r, g, b, a)
            .setUv(0F, 0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(combinedLight)
            .setNormal(0f, 0f, 1f)
        }
        poseStack.popPose()
    }
}
