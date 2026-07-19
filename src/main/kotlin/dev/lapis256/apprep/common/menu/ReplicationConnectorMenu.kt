package dev.lapis256.apprep.common.menu

import appeng.menu.SlotSemantics
import appeng.menu.implementations.UpgradeableMenu
import appeng.menu.slot.AppEngSlot
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogicHost
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.MenuType


/**
 * @see dev.lapis256.apprep.client.gui.ReplicationConnectorScreen
 */
class ReplicationConnectorMenu internal constructor(
    type: MenuType<ReplicationConnectorMenu>,
    id: Int,
    inventory: Inventory,
    host: ReplicationConnectorLogicHost,
) : UpgradeableMenu<ReplicationConnectorLogicHost>(type, id, inventory, host) {
    val returnInventory get() = host.logic.returnInventory

    init {
        val returnInventory = returnInventory.createMenuWrapper()
        for (slot in 0..<returnInventory.size()) {
            this.addSlot(AppEngSlot(returnInventory, slot), SlotSemantics.STORAGE)
        }
    }
}
