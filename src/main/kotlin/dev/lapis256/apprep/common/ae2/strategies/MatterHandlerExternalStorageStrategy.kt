package dev.lapis256.apprep.common.ae2.strategies

import appeng.api.behaviors.ExternalStorageStrategy
import appeng.api.storage.MEStorage
import com.buuz135.replication.ReplicationRegistry
import com.buuz135.replication.api.matter_fluid.IMatterHandler
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.neoforged.neoforge.capabilities.BlockCapabilityCache


@Suppress("UnstableApiUsage")
class MatterHandlerExternalStorageStrategy(
    private val cache: BlockCapabilityCache<IMatterHandler, Direction?>,
) : ExternalStorageStrategy {

    constructor(level: ServerLevel, fromPos: BlockPos, fromSide: Direction) :
        this(BlockCapabilityCache.create(ReplicationRegistry.Capabilities.MATTER_HANDLER, level, fromPos, fromSide))

    override fun createWrapper(extractableOnly: Boolean, injectOrExtractCallback: Runnable): MEStorage? {
        val tank = cache.capability ?: return null
        return MatterHandlerExternalStorageFacade(tank)
    }
}
