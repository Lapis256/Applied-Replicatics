package dev.lapis256.apprep.extension

import appeng.core.definitions.ItemDefinition
import appeng.items.storage.StorageTier
import dev.lapis256.apprep.api.extension.register
import dev.lapis256.apprep.common.item.MatterStorageCell
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ItemLike
import net.neoforged.neoforge.registries.DeferredRegister


fun DeferredRegister.Items.registerStorageCell(tier: StorageTier): MatterCellDefinition {
    return registerStorageCell(tier.namePrefix, "ME", tier)
}

fun DeferredRegister.Items.registerStorageCell(
    englishTierPrefix: String,
    englishCellPrefix: String,
    tier: StorageTier
): MatterCellDefinition {
    return register("$englishTierPrefix $englishCellPrefix Matter Storage Cell", "matter_cell_${tier.namePrefix}") {
        MatterStorageCell(it, tier)
    }.let { MatterCellDefinition(it, tier) }
}

data class MatterCellDefinition(val definition: ItemDefinition<MatterStorageCell>, val tier: StorageTier) : ItemLike {
    override fun asItem(): MatterStorageCell = definition.asItem()
    fun id(): ResourceLocation = definition.id()
}
