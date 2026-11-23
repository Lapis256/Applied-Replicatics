package dev.lapis256.apprep.extension

import appeng.items.storage.StorageTier
import dev.lapis256.apprep.api.extension.ItemDefinition
import dev.lapis256.apprep.api.extension.register
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.common.item.MatterStorageCell
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraftforge.registries.DeferredRegister

fun DeferredRegister<Item>.registerStorageCell(tier: StorageTier): MatterCellDefinition =
    registerStorageCell(tier.namePrefix, "ME", tier, AppRepItems.MATTER_CELL_HOUSING)

fun DeferredRegister<Item>.registerStorageCell(
    englishTierPrefix: String,
    englishCellPrefix: String,
    tier: StorageTier,
    housing: ItemLike,
): MatterCellDefinition =
    register("$englishTierPrefix $englishCellPrefix Matter Storage Cell", "matter_cell_${tier.namePrefix}") {
        MatterStorageCell(it, tier, housing)
    }.let { MatterCellDefinition(it, tier) }

data class MatterCellDefinition(
    val definition: ItemDefinition<MatterStorageCell>,
    val tier: StorageTier,
) : ItemLike {
    override fun asItem(): MatterStorageCell = definition.asItem()

    fun id(): ResourceLocation = definition.id()
}
