package dev.lapis256.apprep.api.ae2.stack

import appeng.api.stacks.AEKey
import appeng.api.stacks.AEKeyType
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.text.AppRepGuiText
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.FriendlyByteBuf

object MatterKeyType : AEKeyType(
    AppliedReplicaticsAPI.rl("m"),
    MatterKey::class.java,
    AppRepGuiText.MATTER.text(),
) {
    override fun loadKeyFromTag(tag: CompoundTag): AEKey? =
        MatterKey.CODEC
            .parse(NbtOps.INSTANCE, tag)
            .result()
            .orElse(null)

    override fun getAmountPerOperation(): Int = 4

    override fun getAmountPerByte(): Int = 256

    override fun getUnitSymbol() = "matter"

    override fun readFromPacket(input: FriendlyByteBuf): AEKey = MatterKey.fromPacket(input)
}
