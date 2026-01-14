package dev.lapis256.apprep.common.ae2.crafting

import appeng.api.crafting.IPatternDetails
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.GenericStack
import com.buuz135.replication.ReplicationRegistry
import dev.lapis256.apprep.common.init.AppRepComponents
import net.minecraft.world.item.ItemStack


class ReplicationPattern(
    private val output: ItemStack,
    private val definitionStack: ItemStack?
) : IPatternDetails {
    constructor(output: ItemStack) : this(output, null)

    private val definition: AEItemKey = run {
        val stack = definitionStack
            ?: ReplicationRegistry.Items.REPLICA_INGOT.get().asItem().defaultInstance
            ?: ItemStack.EMPTY
        stack.set(AppRepComponents.ENCODED_REPLICATION_PATTERN, EncodedReplicationPattern(output))
        AEItemKey.of(stack) ?: error("Definition stack is not a valid AEItemKey")
    }

    override fun getDefinition(): AEItemKey = definition

    private val _inputs = MatterInput.calculateFromOutput(output)
    override fun getInputs(): Array<MatterInput> = _inputs

    override fun getOutputs(): List<GenericStack> =
        GenericStack.fromItemStack(output)?.let {
            listOf(it)
        } ?: emptyList()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return (other as ReplicationPattern).definition == definition
    }

    override fun hashCode(): Int {
        return definition.hashCode()
    }

}
