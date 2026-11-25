package dev.lapis256.apprep.common.item

import appeng.items.storage.BasicStorageCell
import appeng.items.storage.StorageTier
import dev.lapis256.apprep.api.ae2.stack.MatterKeyType
import net.minecraft.world.level.ItemLike

class MatterStorageCell(
    properties: Properties,
    tier: StorageTier,
    housing: ItemLike,
) : BasicStorageCell(
        properties.stacksTo(1),
        tier.componentSupplier.get(),
        housing,
        tier.idleDrain,
        tier.bytes / 1024,
        tier.bytes / 128,
        3,
        MatterKeyType,
    )
