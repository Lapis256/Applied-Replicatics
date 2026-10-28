package dev.lapis256.apprep.common.init

import appeng.api.parts.IPart
import appeng.api.parts.IPartItem
import appeng.core.definitions.ItemDefinition
import appeng.items.parts.PartItem


@Suppress("Unused")
object AppRepParts {


//    val REPLICATION_CONNECTOR = registerPart("ME Replication Connector", "cable_replication_connector", ::ReplicationConnectorPart)


    private inline fun <reified PART : IPart> registerPart(
        englishName: String,
        name: String,
        noinline factory: (IPartItem<PART>) -> PART
    ): ItemDefinition<PartItem<PART>> {
        return AppRepItems.registerPartItem(englishName, name, factory)
    }

    fun init() {
        // No-op
    }
}
