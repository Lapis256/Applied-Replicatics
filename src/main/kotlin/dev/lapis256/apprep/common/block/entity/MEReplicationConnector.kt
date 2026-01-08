package dev.lapis256.apprep.common.block.entity

import appeng.blockentity.grid.AENetworkedBlockEntity
import com.buuz135.replication.api.matter_fluid.IMatterTank
import com.buuz135.replication.api.network.IMatterTanksConsumer
import com.buuz135.replication.api.network.IMatterTanksSupplier
import com.buuz135.replication.network.DefaultMatterNetworkElement
import com.buuz135.replication.network.MatterNetwork
import com.hrznstudio.titanium.block_network.NetworkManager
import dev.lapis256.apprep.api.extension.takeIfServer
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState


class MEReplicationConnector(blockEntityType: BlockEntityType<*>, pos: BlockPos, blockState: BlockState) : AENetworkedBlockEntity(
    blockEntityType,
    pos,
    blockState
), IMatterTanksConsumer, IMatterTanksSupplier {
    override fun getTanks(): List<IMatterTank> {
        return listOf() // MaterTankWrapperList() TODO: 実装する
    }

    override fun getPriority(): Int {
        return 0 // TODO: 実装する
    }

    override fun onLoad() {
        super.onLoad()
        val level = level.takeIfServer() ?: return

        val networkManager = NetworkManager.get(level)
        if (networkManager.getElement(worldPosition) == null) {
            val element = DefaultMatterNetworkElement(level, worldPosition)
            networkManager.addElement(element)
        }
    }

    private var isChunkUnloaded = false
    override fun onChunkUnloaded() {
        isChunkUnloaded = true
    }

    override fun setRemoved() {
        super.setRemoved()

        // チャンクがアンロードされた場合は NetworkElement の削除は行わない
        if (isChunkUnloaded) {
            return
        }

        val level = level.takeIfServer() ?: return
        val networkManager = NetworkManager.get(level)
        val pipe = networkManager.getElement(worldPosition) ?: return
        networkManager.removeElement(worldPosition)

        val network = pipe.getNetwork() as? MatterNetwork ?: return
        network.removeElement(pipe)
    }
}
