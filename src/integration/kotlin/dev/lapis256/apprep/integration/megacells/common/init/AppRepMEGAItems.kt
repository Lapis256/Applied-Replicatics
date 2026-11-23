package dev.lapis256.apprep.integration.megacells.common.init

import appeng.items.AEBaseItem
import appeng.items.materials.MaterialItem
import appeng.items.storage.StorageTier
import dev.lapis256.apprep.api.extension.ItemDefinition
import dev.lapis256.apprep.api.extension.register
import dev.lapis256.apprep.common.init.AppRepCreativeTab
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.extension.registerStorageCell
import dev.lapis256.apprep.integration.megacells.common.MEGACellsIntegration
import gripe._90.megacells.definition.MEGAItems
import net.minecraft.world.item.Item

@Suppress("Unused")
object AppRepMEGAItems {
    val ITEMS = mutableListOf<ItemDefinition<*>>()

    val SKY_REPLICA_INGOT = registerIngredient("Sky Replica Ingot", "sky_replica_ingot") { MaterialItem(it.fireResistant()) }
    val MEGA_MATTER_CELL_HOUSING = registerIngredient("MEGA Matter Cell Housing", "mega_matter_cell_housing")

    val MEGA_CELL_1M = registerStorageCell(MEGAItems.TIER_1M)
    val MEGA_CELL_4M = registerStorageCell(MEGAItems.TIER_4M)
    val MEGA_CELL_16M = registerStorageCell(MEGAItems.TIER_16M)
    val MEGA_CELL_64M = registerStorageCell(MEGAItems.TIER_64M)
    val MEGA_CELL_256M = registerStorageCell(MEGAItems.TIER_256M)

    // Access MEGAItems only once the item registration phase starts.
    internal fun prepareRegistration() = Unit

    private fun registerIngredient(
        englishName: String,
        name: String,
        supplier: (Item.Properties) -> AEBaseItem = ::MaterialItem,
    ) = MEGACellsIntegration.ITEM_REGISTRY
        .register(englishName, name, supplier)
        .also(ITEMS::add)
        .also(AppRepCreativeTab::registerItem)

    private fun registerStorageCell(tier: StorageTier) =
        MEGACellsIntegration.ITEM_REGISTRY
            .registerStorageCell(tier.namePrefix.uppercase(), "MEGA", tier, MEGA_MATTER_CELL_HOUSING)
            .also { ITEMS.add(it.definition) }
            .also(AppRepItems.CELLS::add)
            .also(AppRepCreativeTab::registerItem)
}
