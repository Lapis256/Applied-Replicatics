package dev.lapis256.apprep.api.util

import kotlin.reflect.KProperty


class ResettableLazy<T>(private val initializer: () -> T) {
    @Volatile
    private var value: T? = null

    fun get(): T {
        val current = value
        if (current != null) return current

        synchronized(this) {
            val recheck = value
            if (recheck != null) return recheck
            return initializer().also { value = it }
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = get()

    fun reset() {
        synchronized(this) {
            value = null
        }
    }
}
