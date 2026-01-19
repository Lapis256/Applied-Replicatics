package dev.lapis256.apprep.common.menu

import appeng.menu.AEBaseMenu
import appeng.menu.SlotSemantics
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
    host: ReplicationConnectorLogicHost
) : AEBaseMenu(type, id, inventory, host) {
    private val logic = host.logic
    val returnInventory = logic.returnInventory

    init {
        createPlayerInventorySlots(inventory)

        val returnInventory = returnInventory.createMenuWrapper()
        for (slot in 0..<returnInventory.size()) {
            this.addSlot(AppEngSlot(returnInventory, slot), SlotSemantics.STORAGE)
        }
    }
}
