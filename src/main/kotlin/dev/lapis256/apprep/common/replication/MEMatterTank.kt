package dev.lapis256.apprep.common.replication

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.storage.MEStorage
import com.buuz135.replication.api.IMatterType
import com.buuz135.replication.api.matter_fluid.IMatterTank
import com.buuz135.replication.api.matter_fluid.MatterStack
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import net.neoforged.neoforge.fluids.capability.IFluidHandler


/**
 * ME ネットワークのマターを扱う [IMatterTank] 実装
 */
class MEMatterTank(
    private val type: IMatterType,
    private var amount: Long,
    private val storage: MEStorage,
    private val source: IActionSource
) : IMatterTank {
    private val doubleAmount get() = amount.toDouble()
    private val stack get() = MatterStack(type, doubleAmount)

    override fun getMatter(): MatterStack = stack
    override fun getMatterAmount() = doubleAmount
    override fun getCapacity() = Long.MAX_VALUE.toDouble()
    override fun isMatterValid(stack: MatterStack?): Boolean = true

    fun updateAmount(newAmount: Long) {
        amount = newAmount
    }

    /**
     * マターを [MEStorage] へ搬入する。
     *
     * TODO: StorageHelper を使ってエネルギーコストを考慮した挿入処理を実装すると良いかも?
     */
    override fun fill(resource: MatterStack, action: IFluidHandler.FluidAction): Double {
        if (resource.isEmpty || resource.matterType != type) {
            return 0.0
        }
        return storage.insert(MatterKey.of(type), resource.amount.toLong(), Actionable.of(action), source).toDouble()
    }

    /**
     * マターを [MEStorage] から搬出する。
     * TODO: StorageHelper を使ってエネルギーコストを考慮した挿入処理を実装すると良いかも?
     */
    override fun drain(maxDrain: Double, action: IFluidHandler.FluidAction): MatterStack {
        val extracted = storage.extract(MatterKey.of(type), maxDrain.toLong(), Actionable.of(action), source)
        return MatterStack(type, extracted.toDouble())
    }

    /**
     * マターを [MEStorage] から搬出する。
     * TODO: StorageHelper を使ってエネルギーコストを考慮した挿入処理を実装すると良いかも?
     */
    override fun drain(resource: MatterStack, action: IFluidHandler.FluidAction): MatterStack {
        if (resource.isEmpty || resource.matterType != type) {
            return MatterStack.EMPTY
        }
        return drain(resource.amount, action)
    }
}
