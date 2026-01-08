package dev.lapis256.apprep.common.item

import appeng.items.storage.BasicStorageCell
import appeng.items.storage.StorageTier
import dev.lapis256.apprep.api.ae2.MatterKeyType


class MatterStorageCell(properties: Properties, tier: StorageTier) :
    BasicStorageCell(
        properties.stacksTo(1),
        tier.idleDrain,
        tier.bytes / 1024,
        tier.bytes / 128,
        3,
        MatterKeyType
    )
