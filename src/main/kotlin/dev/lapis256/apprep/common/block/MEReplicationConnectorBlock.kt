package dev.lapis256.apprep.common.block

import appeng.block.AEBaseEntityBlock
import com.hrznstudio.titanium.block_network.INetworkDirectionalConnection
import dev.lapis256.apprep.common.block.entity.MEReplicationConnector
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState


class MEReplicationConnectorBlock : AEBaseEntityBlock<MEReplicationConnector>(metalProps()), INetworkDirectionalConnection {
    override fun canConnect(level: Level?, pos: BlockPos?, state: BlockState?, side: Direction?) = true
}
