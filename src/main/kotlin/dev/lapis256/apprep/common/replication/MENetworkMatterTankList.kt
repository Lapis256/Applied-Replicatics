package dev.lapis256.apprep.common.replication

import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEKey
import appeng.api.storage.MEStorage
import appeng.me.storage.NullInventory
import com.buuz135.replication.api.IMatterType
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.util.ResettableLazy
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap


/**
 * ME ネットワーク内のすべてのマターをタンクのリストとして扱う為のクラス
 */
class MENetworkMatterTankList(
    cachedMatters: Map<IMatterType, Long>,
    private val storage: MEStorage,
    private val source: IActionSource
) : List<MEMatterTank> {
    companion object {
        fun empty(): MENetworkMatterTankList {
            return MENetworkMatterTankList(
                mapOf(),
                NullInventory.of(),
                IActionSource.empty()
            )
        }
    }

    private fun createTank(type: IMatterType, amount: Long): MEMatterTank {
        val stack = MatterStack(type, amount.toDouble())
        return MEMatterTank(stack, storage, source)
    }

    private val tankMap = Object2ObjectOpenHashMap<IMatterType, MEMatterTank>().apply {
        cachedMatters.forEach { (type, amount) ->
            put(type, createTank(type, amount))
        }
    }

    /**
     * タンク内のマター量を更新する。
     * AE2 のストレージは Long で数量を扱うため、Long を受け取って Double に変換する。
     */
    fun updateCache(key: AEKey, amount: Long) {
        val matterKey = key as? MatterKey ?: return
        val type = matterKey.type

        if (amount <= 0) {
            tankMap.remove(type)?.let {
                _tankList.reset()
            }
            return
        }

        val tank = tankMap[type]
        if (tank != null) {
            tank.updateAmount(amount)
        } else {
            tankMap[type] = createTank(type, amount)
            _tankList.reset()
        }
    }

    private val _tankList = ResettableLazy { tankMap.values.toList() }
    private val tankList by _tankList

    override val size get() = tankList.size
    override fun isEmpty() = tankList.isEmpty()
    override fun contains(element: MEMatterTank) = tankList.contains(element)
    override fun iterator() = tankList.iterator()
    override fun containsAll(elements: Collection<MEMatterTank>) = tankList.containsAll(elements)
    override fun get(index: Int): MEMatterTank = tankList[index]
    override fun indexOf(element: MEMatterTank) = tankList.indexOf(element)
    override fun lastIndexOf(element: MEMatterTank) = tankList.lastIndexOf(element)
    override fun listIterator() = tankList.listIterator()
    override fun listIterator(index: Int) = tankList.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int) = tankList.subList(fromIndex, toIndex)
}
