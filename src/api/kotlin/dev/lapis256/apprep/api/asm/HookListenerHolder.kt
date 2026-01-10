package dev.lapis256.apprep.api.asm


interface HookListenerHolder<LISTENER : HookListener> {
    fun `apprep$addListener`(listener: LISTENER): Boolean

    fun addListener(listener: LISTENER) = `apprep$addListener`(listener)

    fun `apprep$removeListener`(listener: LISTENER): Boolean

    fun removeListener(listener: LISTENER) = `apprep$removeListener`(listener)
}
