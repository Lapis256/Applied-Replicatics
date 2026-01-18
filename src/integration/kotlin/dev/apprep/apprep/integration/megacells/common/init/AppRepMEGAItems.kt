package dev.apprep.apprep.integration.megacells.common.init

import appeng.core.definitions.ItemDefinition
import appeng.items.AEBaseItem
import appeng.items.materials.MaterialItem
import appeng.items.storage.StorageTier
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.extension.register
import dev.lapis256.apprep.common.init.AppRepCreativeTab
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.extension.registerStorageCell
import gripe._90.megacells.definition.MEGAItems
import net.minecraft.world.item.Item
import net.neoforged.neoforge.registries.DeferredRegister


@Suppress("Unused")
object AppRepMEGAItems {
    val REGISTRY: DeferredRegister.Items = DeferredRegister.createItems(AppliedReplicaticsAPI.MOD_ID)

    val ITEMS = mutableListOf<ItemDefinition<*>>()


    val SKY_REPLICA_INGOT = registerIngredient("Sky Replica Ingot", "sky_replica_ingot") { MaterialItem(it.fireResistant()) }
    val MEGA_MATTER_CELL_HOUSING = registerIngredient("MEGA Matter Cell Housing", "mega_matter_cell_housing")

    val MEGA_CELL_1M = registerStorageCell(MEGAItems.TIER_1M)
    val MEGA_CELL_4M = registerStorageCell(MEGAItems.TIER_4M)
    val MEGA_CELL_16M = registerStorageCell(MEGAItems.TIER_16M)
    val MEGA_CELL_64M = registerStorageCell(MEGAItems.TIER_64M)
    val MEGA_CELL_256M = registerStorageCell(MEGAItems.TIER_256M)


    private fun registerIngredient(englishName: String, name: String, supplier: (Item.Properties) -> AEBaseItem = ::MaterialItem) = REGISTRY
        .register(englishName, name, supplier)
        .also(ITEMS::add)
        .also(AppRepCreativeTab::registerItem)

    private fun registerStorageCell(tier: StorageTier) =
        REGISTRY.registerStorageCell(tier.namePrefix.uppercase(), "MEGA", tier)
            .also { ITEMS.add(it.definition) }
            .also(AppRepItems.CELLS::add)
            .also(AppRepCreativeTab::registerItem)
}
