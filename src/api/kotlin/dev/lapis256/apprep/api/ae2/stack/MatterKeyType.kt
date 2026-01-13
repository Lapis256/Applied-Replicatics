package dev.lapis256.apprep.api.ae2.stack

import appeng.api.stacks.AEKey
import appeng.api.stacks.AEKeyType
import com.mojang.serialization.MapCodec
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.api.text.AppRepGuiText
import net.minecraft.network.RegistryFriendlyByteBuf


object MatterKeyType : AEKeyType(
    AppliedReplicaticsAPI.rl("m"),
    MatterKey::class.java,
    AppRepGuiText.MATTER.text()
) {
    override fun codec(): MapCodec<out AEKey> = MatterKey.MAP_CODEC

    override fun getAmountPerOperation(): Int = 4

    override fun getAmountPerByte(): Int = 256

    override fun getUnitSymbol() = "matter"

    override fun readFromPacket(input: RegistryFriendlyByteBuf): AEKey =
        MatterKey.fromPacket(input)
}
