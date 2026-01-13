package dev.lapis256.apprep.common.ae2.storage

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.AEKey
import appeng.api.stacks.KeyCounter
import appeng.api.storage.MEStorage
import com.buuz135.replication.network.MatterNetwork
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import dev.lapis256.apprep.api.text.AppRepGuiText
import dev.lapis256.apprep.common.ae2.storage.cache.MatterNetworkStorageCache
import net.minecraft.network.chat.Component


class MatterNetworkStorage(matterNetwork: MatterNetwork) : MEStorage {
    private val cache = MatterNetworkStorageCache(matterNetwork)
    private val tankOperations = GroupedMatterTanks { cache.cachedTanks }

    fun invalidateStacks() {
        cache.invalidateStacks()
    }

    fun invalidateAll() {
        cache.invalidateAll()
        tankOperations.invalidate()
    }

    override fun getDescription(): Component = AppRepGuiText.MATTER_NETWORK_STORAGE.text()

    override fun insert(what: AEKey, amount: Long, mode: Actionable, source: IActionSource): Long {
        val matterKey = what as? MatterKey ?: return 0L
        return tankOperations.insert(matterKey, amount, mode)
    }

    override fun extract(what: AEKey, amount: Long, mode: Actionable, source: IActionSource): Long {
        val matterKey = what as? MatterKey ?: return 0L
        return tankOperations.extract(matterKey, amount, mode)
    }

    override fun getAvailableStacks(out: KeyCounter) {
        out.addAll(cache.cachedStacks)
    }
}
