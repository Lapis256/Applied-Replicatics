package dev.lapis256.apprep.common.ae2.storage

import appeng.api.config.Actionable
import com.buuz135.replication.api.IMatterType
import com.buuz135.replication.api.matter_fluid.MatterTank
import dev.lapis256.apprep.api.ae2.MatterKey
import dev.lapis256.apprep.api.util.ResettableLazy
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import java.util.function.Supplier
import kotlin.math.roundToLong


class GroupedMatterTanks(
    private val tanksSupplier: Supplier<Collection<MatterTank>>
) {
    private val _groupedTanks = ResettableLazy { groupTanksByType() }
    private val groupedTanks by _groupedTanks

    private val _emptyTanks = ResettableLazy {
        tanksSupplier.get()
            .filter { it.matter.isEmpty }
            .toCollection(ObjectLinkedOpenHashSet())
    }
    private val emptyTanks by _emptyTanks

    private fun groupTanksByType() = Object2ObjectLinkedOpenHashMap<IMatterType, ObjectLinkedOpenHashSet<MatterTank>>()
        .apply {
            tanksSupplier.get()
                .filterNot { it.matter.isEmpty }
                .forEach { tank ->
                    this.computeIfAbsent(tank.matter.matterType) { ObjectLinkedOpenHashSet() }
                        .add(tank)
                }
        }

    fun invalidate() {
        _groupedTanks.reset()
        _emptyTanks.reset()
    }

    fun insert(key: MatterKey, amount: Long, mode: Actionable): Long {
        val type = key.stack.matterType
        var remaining = amount

        groupedTanks[type]?.forEach { tank ->
            val inserted = tank.fill(key.toStack(remaining), mode.fluidAction)
            remaining -= inserted.roundToLong()

            if (remaining <= 0L) {
                return amount - remaining
            }
        }

        val iterator = emptyTanks.iterator()
        while (iterator.hasNext() && remaining > 0L) {
            val tank = iterator.next()
            val inserted = tank.fill(key.toStack(remaining), mode.fluidAction)
            if (inserted > 0.0) {
                iterator.remove()
                groupedTanks.computeIfAbsent(type) { ObjectLinkedOpenHashSet() }.add(tank)
            }
            remaining -= inserted.roundToLong()
        }

        return amount - remaining
    }

    fun extract(key: MatterKey, amount: Long, mode: Actionable): Long {
        val type = key.stack.matterType
        val tanks = groupedTanks[type] ?: return 0L

        var remaining = amount
        val iterator = tanks.iterator()
        while (iterator.hasNext() && remaining > 0L) {
            val tank = iterator.next()
            val extracted = tank.drain(key.toStack(remaining), mode.fluidAction).amount.toLong()
            if (tank.isEmpty) {
                iterator.remove()
                emptyTanks.add(tank)
            }
            remaining -= extracted
        }

        return amount - remaining
    }
}
