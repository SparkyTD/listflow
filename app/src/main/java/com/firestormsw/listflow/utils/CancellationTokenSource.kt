package com.firestormsw.listflow.utils

class CancellationTokenSource {
    private val cancellationToken = CancellationToken()

    fun getToken(): CancellationToken {
        return cancellationToken
    }

    fun cancel() {
        cancellationToken.cancel()
    }

    companion object {
        fun createLinkedTokenSource(vararg tokens: CancellationToken): CancellationTokenSource {
            val source = CancellationTokenSource()
            for (token in tokens) {
                token.registerOnCanceledListener {
                    source.cancel()
                }
            }
            return source
        }
    }
}