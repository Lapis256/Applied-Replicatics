package dev.lapis256.apprep.common.block

import appeng.block.AEBaseEntityBlock
import appeng.menu.locator.MenuLocators
import appeng.util.InteractionUtil
import com.hrznstudio.titanium.block_network.INetworkDirectionalConnection
import dev.lapis256.apprep.common.block.entity.ReplicationConnectorBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class ReplicationConnectorBlock :
    AEBaseEntityBlock<ReplicationConnectorBlockEntity>(metalProps()),
    INetworkDirectionalConnection {
    override fun canConnect(
        state: BlockState?,
        side: Direction?,
    ) = true

    override fun onActivated(
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        heldItem: ItemStack?,
        hitResult: BlockHitResult,
    ): InteractionResult {
        if (InteractionUtil.isInAlternateUseMode(player)) return InteractionResult.PASS

        val blockEntity = getBlockEntity(level, pos) ?: return InteractionResult.PASS

        if (!level.isClientSide) {
            blockEntity.openMenu(player, MenuLocators.forBlockEntity(blockEntity))
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }
}
