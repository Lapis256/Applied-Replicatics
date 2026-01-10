package dev.lapis256.apprep.api.asm

import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet


abstract class HookListenerHolderForImpl<LISTENER : HookListener> {
    private val listeners: ObjectLinkedOpenHashSet<LISTENER> = ObjectLinkedOpenHashSet()

    fun addListener(listener: LISTENER): Boolean =
        listeners.add(listener)
            .also { AppliedReplicaticsAPI.LOGGER.info("Added listener $listener, listeners: ${listeners.joinToString { it.toString() }}") }

    fun removeListener(listener: LISTENER): Boolean =
        listeners.remove(listener).also { AppliedReplicaticsAPI.LOGGER.info("Removed listener $listener, listeners: ${listeners.joinToString { it.toString() }}") }

    protected fun fire(handler: (LISTENER) -> Unit) {
        listeners.forEach(handler)
    }
}
