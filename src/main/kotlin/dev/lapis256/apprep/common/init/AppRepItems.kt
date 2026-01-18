package dev.lapis256.apprep.common.init

import appeng.api.parts.IPart
import appeng.api.parts.IPartItem
import appeng.core.definitions.ItemDefinition
import appeng.items.AEBaseItem
import appeng.items.materials.MaterialItem
import appeng.items.parts.PartItem
import appeng.items.storage.StorageTier
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.extension.register
import dev.lapis256.apprep.extension.MatterCellDefinition
import dev.lapis256.apprep.extension.registerStorageCell
import net.minecraft.world.item.Item
import net.neoforged.neoforge.registries.DeferredRegister


@Suppress("Unused")
object AppRepItems {
    val REGISTRY: DeferredRegister.Items = DeferredRegister.createItems(AppliedReplicaticsAPI.MOD_ID)

    val ITEMS = mutableListOf<ItemDefinition<*>>()
    val CELLS = mutableListOf<MatterCellDefinition>()


    val MATTER_CELL_HOUSING = registerIngredient("ME Matter Cell Housing", "matter_cell_housing")

    val MATTER_CELL_1K = registerStorageCell(StorageTier.SIZE_1K)
    val MATTER_CELL_4K = registerStorageCell(StorageTier.SIZE_4K)
    val MATTER_CELL_16K = registerStorageCell(StorageTier.SIZE_16K)
    val MATTER_CELL_64K = registerStorageCell(StorageTier.SIZE_64K)
    val MATTER_CELL_256K = registerStorageCell(StorageTier.SIZE_256K)


    private fun registerIngredient(englishName: String, name: String, supplier: (Item.Properties) -> AEBaseItem = ::MaterialItem) = REGISTRY
        .register(englishName, name, supplier)
        .also(ITEMS::add)
        .also(AppRepCreativeTab::registerItem)

    private fun registerStorageCell(tier: StorageTier) = REGISTRY
        .registerStorageCell(tier)
        .also { ITEMS.add(it.definition) }
        .also(CELLS::add)
        .also(AppRepCreativeTab::registerItem)

    internal inline fun <reified PART : IPart> registerPartItem(
        englishName: String,
        name: String,
        noinline factory: (IPartItem<PART>) -> PART
    ) = REGISTRY.register(englishName, name) { PartItem(it, PART::class.java, factory) }
        .also(ITEMS::add)
        .also(AppRepCreativeTab::registerItem)
}
