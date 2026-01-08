package dev.lapis256.apprep.client

import appeng.api.client.AEKeyRenderHandler
import com.buuz135.replication.Replication
import com.mojang.blaze3d.vertex.PoseStack
import dev.lapis256.apprep.api.ae2.MatterKey
import dev.lapis256.apprep.api.util.renderMatterTypeOnGui
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level


class AE2MatterStackRenderer : AEKeyRenderHandler<MatterKey> {
    override fun getDisplayName(stack: MatterKey): Component = stack.displayName

    override fun drawInGui(minecraft: Minecraft, guiGraphics: GuiGraphics, x: Int, y: Int, stack: MatterKey) {
        renderMatterTypeOnGui(guiGraphics, stack.stack.matterType, x, y)
    }

    override fun drawOnBlockFace(poseStack: PoseStack, buffers: MultiBufferSource, stack: MatterKey, scale: Float, combinedLight: Int, level: Level) {
        val type = stack.stack.matterType
        val (r, g, b, a) = type.color.get()
        val icon = ResourceLocation.fromNamespaceAndPath(Replication.MOD_ID, "textures/gui/mattertypes/${type.name.lowercase()}.png")

        // The following implementation is based on appeng.init.client.InitStackRenderHandlers.FluidKeyRenderHandler#drawOnBlockFace.
        // https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/74ed04a0ffe055ddf2ae06a91cf43c63292a3af0/src/main/java/appeng/init/client/InitStackRenderHandlers.java#L125-L166
        poseStack.pushPose()
        // Push it out of the block face a bit to avoid z-fighting
        poseStack.translate(0f, 0f, 0.001f)

        val buffer = buffers.getBuffer(RenderType.text(icon))

        // y is flipped here
        val x0 = -scale / 2F
        val y0 = scale / 2F
        val x1 = scale / 2F
        val y1 = -scale / 2F

        val transform = poseStack.last().pose()
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
        poseStack.popPose()
    }
}
