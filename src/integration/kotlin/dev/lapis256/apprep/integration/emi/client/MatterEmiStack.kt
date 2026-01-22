package dev.lapis256.apprep.integration.emi.client

import com.buuz135.replication.api.IMatterType
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.emi.emi.api.render.EmiRender
import dev.emi.emi.api.render.EmiTooltipComponents
import dev.emi.emi.api.stack.EmiStack
import dev.lapis256.apprep.api.replication.util.getMatterId
import dev.lapis256.apprep.api.replication.util.renderMatterTypeOnGui
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation


class MatterEmiStack(val stack: MatterStack, amount: Long) : EmiStack() {
    constructor(type: IMatterType, amount: Long) :
            this(MatterStack(type, 1.0), amount)

    init {
        setAmount(amount)
    }

    override fun copy(): EmiStack {
        return MatterEmiStack(stack, amount).also { result ->
            result.chance = this.chance
            result.remainder = this.remainder
            result.comparison = this.comparison
        }
    }

    override fun isEmpty() = stack.isEmpty

    override fun getComponentChanges(): DataComponentPatch = DataComponentPatch.EMPTY

    override fun getKey(): Any? = stack.matterType

    override fun getId(): ResourceLocation = getMatterId(stack.matterType)

    override fun getTooltipText(): List<Component> {
        return if (isEmpty) {
            emptyList()
        } else {
            listOf(stack.displayName)
        }
    }

    override fun getTooltip(): List<ClientTooltipComponent> {
        return buildList {
            tooltipText.forEach { text ->
                add(ClientTooltipComponent.create(text.visualOrderText))
            }
            EmiTooltipComponents.appendModName(this, id.namespace)
            addAll(super.getTooltip())
        }
    }

    override fun getName(): Component = stack.displayName

    override fun render(graphics: GuiGraphics, x: Int, y: Int, delta: Float, flags: Int) {
        renderMatterTypeOnGui(graphics, stack.matterType, x, y)
        if ((flags and RENDER_REMAINDER) != 0) {
            EmiRender.renderRemainderIcon(this, graphics, x, y)
        }
    }
}
