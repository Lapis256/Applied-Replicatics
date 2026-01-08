package dev.apprep.apprep.integration.megacells.common.init

import appeng.core.definitions.ItemDefinition
import appeng.items.storage.StorageTier
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.init.AppRepCreativeTab
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.extension.registerStorageCell
import gripe._90.megacells.definition.MEGAItems
import net.neoforged.neoforge.registries.DeferredRegister


@Suppress("Unused")
object AppRepMEGAItems {
    val REGISTRY: DeferredRegister.Items = DeferredRegister.createItems(AppliedReplicaticsAPI.MOD_ID)

    val ITEMS = mutableListOf<ItemDefinition<*>>()


    val MEGA_CELL_1M = registerStorageCell(MEGAItems.TIER_1M)
    val MEGA_CELL_4M = registerStorageCell(MEGAItems.TIER_4M)
    val MEGA_CELL_16M = registerStorageCell(MEGAItems.TIER_16M)
    val MEGA_CELL_64M = registerStorageCell(MEGAItems.TIER_64M)
    val MEGA_CELL_256M = registerStorageCell(MEGAItems.TIER_256M)


    private fun registerStorageCell(tier: StorageTier) =
        REGISTRY.registerStorageCell(tier.namePrefix.uppercase(), "MEGA", tier)
            .also(ITEMS::add)
            .also(AppRepItems.CELLS::add)
            .also(AppRepCreativeTab::registerItem)
}
