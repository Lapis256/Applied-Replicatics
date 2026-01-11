package dev.lapis256.apprep.common.ae2.storage.cache

import appeng.api.stacks.KeyCounter
import com.buuz135.replication.api.matter_fluid.MatterTank
import com.buuz135.replication.api.network.IMatterTanksSupplier
import com.buuz135.replication.network.MatterNetwork
import dev.lapis256.apprep.api.ae2.MatterKey
import dev.lapis256.apprep.api.util.ResettableLazy
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogicHost
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import kotlin.math.roundToLong


class MatterNetworkStorageCache(
    private val matterNetwork: MatterNetwork
) {
    private fun collectTanks(): ObjectLinkedOpenHashSet<MatterTank> {
        val loadedSuppliers = matterNetwork.matterStacksHolders
            .filter { it.level.isLoaded(it.pos) }
            .mapNotNull { it.level.getBlockEntity(it.pos) as? IMatterTanksSupplier }
            .filterNot { it is ReplicationConnectorLogicHost }
            .sortedBy(IMatterTanksSupplier::getPriority)

        return loadedSuppliers
            .flatMap(IMatterTanksSupplier::getTanks)
            .filterIsInstance<MatterTank>()
            .toCollection(ObjectLinkedOpenHashSet())
    }

    private val _cachedTanks = ResettableLazy { collectTanks() }
    val cachedTanks by _cachedTanks

    private fun collectStacks(): KeyCounter =
        KeyCounter().apply {
            cachedTanks
                .forEach { tank ->
                    val matterStack = tank.matter
                    val key = MatterKey.of(matterStack.matterType)
                    this[key] += matterStack.amount.roundToLong()
                }
        }

    private val _cachedStacks = ResettableLazy { collectStacks() }
    val cachedStacks by _cachedStacks

    fun invalidateStacks() {
        _cachedStacks.reset()
    }

    fun invalidateAll() {
        _cachedTanks.reset()
        _cachedStacks.reset()
    }
}
