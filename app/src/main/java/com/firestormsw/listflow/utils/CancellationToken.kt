package com.firestormsw.listflow.utils

class CancellationToken {
    @Volatile
    private var isCanceled: Boolean = false
    private val listeners = mutableListOf<() -> Unit>()

    @Synchronized
    fun registerOnCanceledListener(listener: () -> Unit) {
        if (isCanceled) {
            listener.invoke()
        } else {
            listeners.add(listener)
        }
    }

    internal fun cancel() {
        if (!isCanceled) {
            isCanceled = true
            notifyListeners()
        }
    }

    @Synchronized
    private fun notifyListeners() {
        listeners.forEach { it.invoke() }
        listeners.clear()
    }

    fun isCancellationRequested(): Boolean {
        return isCanceled
    }
}