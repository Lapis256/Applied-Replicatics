package dev.lapis256.apprep.api.extension

import net.minecraft.world.level.Level


fun Level?.takeIfClient(): Level? = this?.takeIf { it.isClientSide }
fun Level?.takeIfServer(): Level? = this?.takeIf { !it.isClientSide }
