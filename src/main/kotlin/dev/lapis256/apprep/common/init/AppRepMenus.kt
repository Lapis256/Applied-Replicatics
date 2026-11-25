package dev.lapis256.apprep.common.init

import appeng.menu.implementations.MenuTypeBuilder
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogicHost
import dev.lapis256.apprep.common.menu.ReplicationConnectorMenu

object AppRepMenus {
    val REPLICATION_CONNECTOR =
        MenuTypeBuilder
            .create(MenuTypeBuilder.TypedMenuFactory(::ReplicationConnectorMenu), ReplicationConnectorLogicHost::class.java)
            .build("apprep_replication_connector")
}
