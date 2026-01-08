package dev.lapis256.apprep.common.ae2

import appeng.api.behaviors.ContainerItemStrategy
import appeng.api.config.Actionable
import appeng.api.stacks.GenericStack
import dev.lapis256.apprep.api.ae2.MatterKey
import dev.lapis256.apprep.api.ae2.toGenericStackOrNull
import dev.lapis256.apprep.common.util.ItemMatterTankWrapper
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack


@Suppress("UnstableApiUsage")
object MatterTankItemStrategy : ContainerItemStrategy<MatterKey, MatterTankItemStrategy.Context> {

    override fun getContainedStack(stack: ItemStack): GenericStack? {
        val wrapper = ItemMatterTankWrapper.of(stack) ?: return null
        return wrapper.matter.toGenericStackOrNull()
    }

    override fun findCarriedContext(player: Player, menu: AbstractContainerMenu): Context? {
        if (ItemMatterTankWrapper.isMatterTank(menu.carried)) {
            return Context.Carried(player, menu)
        }
        return null
    }

    override fun findPlayerSlotContext(player: Player, slot: Int): Context? {
        val slotStack = player.inventory.getItem(slot)
        if (ItemMatterTankWrapper.isMatterTank(slotStack)) {
            return Context.PlayerInventory(player, slot)
        }
        return null
    }

    override fun extract(context: Context, what: MatterKey, amount: Long, mode: Actionable): Long {
        val stack = context.stack
        val copy = stack.copyWithCount(1)
        val wrapper = ItemMatterTankWrapper.of(copy) ?: return 0L

        val extracted: Long = wrapper.drain(what.toStack(amount), mode.fluidAction).amount.toLong()
        if (mode == Actionable.MODULATE) {
            stack.shrink(1)
            context.addOverflow(wrapper.getChangedStack())
        }
        return extracted
    }

    override fun insert(context: Context, what: MatterKey, amount: Long, mode: Actionable): Long {
        val stack = context.stack
        val copy = stack.copyWithCount(1)
        val wrapper = ItemMatterTankWrapper.of(copy) ?: return 0L

        val filled = wrapper.fill(what.toStack(amount), mode.fluidAction)
        if (mode == Actionable.MODULATE) {
            stack.shrink(1)
            context.addOverflow(wrapper.getChangedStack())
        }
        return filled.toLong()
    }

    override fun playFillSound(player: Player, what: MatterKey) {
        player.playNotifySound(SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    override fun playEmptySound(player: Player, what: MatterKey) {
        player.playNotifySound(SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    override fun getExtractableContent(context: Context): GenericStack? {
        return getContainedStack(context.stack)
    }

    sealed class Context(open val player: Player) {
        abstract var stack: ItemStack

        open fun addOverflow(stack: ItemStack) {
            player.inventory.placeItemBackInInventory(stack)
        }

        data class Carried(override val player: Player, val menu: AbstractContainerMenu) : Context(player) {
            override var stack: ItemStack
                get() = menu.carried
                set(value) {
                    menu.carried = value
                }

            override fun addOverflow(stack: ItemStack) {
                if (menu.carried.isEmpty) {
                    menu.carried = stack
                } else {
                    super.addOverflow(stack)
                }
            }
        }

        data class PlayerInventory(override val player: Player, val slot: Int) : Context(player) {
            override var stack: ItemStack
                get() = player.inventory.getItem(slot)
                set(value) {
                    player.inventory.setItem(slot, value)
                }
        }
    }


}
