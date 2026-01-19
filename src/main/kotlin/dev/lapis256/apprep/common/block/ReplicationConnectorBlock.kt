package dev.lapis256.apprep.common.block

import appeng.block.AEBaseEntityBlock
import appeng.menu.locator.MenuLocators
import com.hrznstudio.titanium.block_network.INetworkDirectionalConnection
import dev.lapis256.apprep.common.block.entity.ReplicationConnectorBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult


class ReplicationConnectorBlock : AEBaseEntityBlock<ReplicationConnectorBlockEntity>(metalProps()), INetworkDirectionalConnection {
    override fun canConnect(level: Level?, pos: BlockPos?, state: BlockState?, side: Direction?) = true

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        val blockEntity = level.getBlockEntity(pos) as? ReplicationConnectorBlockEntity
            ?: return super.useWithoutItem(state, level, pos, player, hitResult)

        if (!level.isClientSide) {
            blockEntity.openMenu(player, MenuLocators.forBlockEntity(blockEntity))
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }
}
