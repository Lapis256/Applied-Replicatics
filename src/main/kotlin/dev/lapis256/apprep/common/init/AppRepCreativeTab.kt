package dev.lapis256.apprep.common.init

import appeng.api.ids.AECreativeTabIds
import com.buuz135.replication.Replication
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.text.AppRepGuiText
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister


@Suppress("Unused")
object AppRepCreativeTab {
    val REGISTRY: DeferredRegister<CreativeModeTab> = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AppliedReplicaticsAPI.MOD_ID)

    private val displayEntries = mutableListOf<TabEntry>()

    fun registerItem(item: ItemLike) {
        displayEntries.add(TabEntry.Item(item))
    }

    fun registerItem(items: Collection<ItemLike>) {
        displayEntries.addAll(items.map(TabEntry::Item))
    }

    fun registerStack(stack: ItemStack) {
        displayEntries.add(TabEntry.Stack(stack))
    }

    fun registerStack(stacks: Collection<ItemStack>) {
        displayEntries.addAll(stacks.map(TabEntry::Stack))
    }

    val MAIN: DeferredHolder<CreativeModeTab, CreativeModeTab> = REGISTRY.register("main") { ->
        CreativeModeTab.builder()
            .title(AppRepGuiText.CREATIVE_TAB.text())
            .icon { ItemStack(AppRepItems.MATTER_CELL_256k.get()) }
            .withTabsBefore(AECreativeTabIds.MAIN, ResourceKey.create(Registries.CREATIVE_MODE_TAB, Replication.TAB.resourceLocation))
            .displayItems { parameters, output ->
                for (entry in displayEntries) {
                    entry.register(output)
                }
            }
            .build()
    }

    private sealed class TabEntry(private val registerer: (CreativeModeTab.Output) -> Unit) {
        fun register(output: CreativeModeTab.Output) = registerer(output)

        class Item(item: ItemLike) : TabEntry({ it.accept(item) })
        class Stack(stack: ItemStack) : TabEntry({ it.accept(stack) })
    }
}
