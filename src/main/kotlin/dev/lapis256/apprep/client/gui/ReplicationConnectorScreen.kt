package dev.lapis256.apprep.client.gui

import appeng.client.gui.AEBaseScreen
import appeng.client.gui.style.ScreenStyle
import dev.lapis256.apprep.common.menu.ReplicationConnectorMenu
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory


class ReplicationConnectorScreen(menu: ReplicationConnectorMenu, inventory: Inventory, title: Component, style: ScreenStyle) :
    AEBaseScreen<ReplicationConnectorMenu>(menu, inventory, title, style) {

    init {
        widgets.addOpenPriorityButton()
    }
}
