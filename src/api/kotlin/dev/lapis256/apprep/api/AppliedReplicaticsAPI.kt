package dev.lapis256.apprep.api

import com.mojang.logging.LogUtils
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import java.util.*


object AppliedReplicaticsAPI {
    const val MOD_ID = "apprep"

    @JvmField
    val LOGGER: Logger = LogUtils.getLogger();

    @JvmStatic
    fun rl(path: String): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath(MOD_ID, path)

    internal inline fun <reified S> loadService(): S =
        ServiceLoader.load(S::class.java, this::class.java.classLoader).first()
}
