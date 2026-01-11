package dev.lapis256.apprep.api.asm

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet


abstract class HookListenerHolderForImpl<LISTENER : HookListener> {
    private val listeners: ObjectLinkedOpenHashSet<LISTENER> = ObjectLinkedOpenHashSet()

    fun addListener(listener: LISTENER): Boolean = listeners.add(listener)

    fun removeListener(listener: LISTENER): Boolean = listeners.remove(listener)

    protected fun fire(handler: (LISTENER) -> Unit) {
        listeners.forEach(handler)
    }
}
