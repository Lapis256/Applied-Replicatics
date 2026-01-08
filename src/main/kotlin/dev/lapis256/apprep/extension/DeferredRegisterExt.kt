package dev.lapis256.apprep.extension

import appeng.core.definitions.ItemDefinition
import appeng.items.storage.StorageTier
import dev.lapis256.apprep.common.item.MatterStorageCell
import dev.lapis256.apprep.api.extension.register
import net.neoforged.neoforge.registries.DeferredRegister


fun DeferredRegister.Items.registerStorageCell(tier: StorageTier): ItemDefinition<MatterStorageCell> {
    return registerStorageCell(tier.namePrefix, "ME", tier)
}

fun DeferredRegister.Items.registerStorageCell(
    englishTierPrefix: String,
    englishCellPrefix: String,
    tier: StorageTier
): ItemDefinition<MatterStorageCell> {
    return register("$englishTierPrefix $englishCellPrefix Matter Storage Cell", "matter_cell_${tier.namePrefix}") {
        MatterStorageCell(it, tier)
    }
}
