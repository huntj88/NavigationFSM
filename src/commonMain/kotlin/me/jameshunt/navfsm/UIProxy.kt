package me.jameshunt.navfsm

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

interface UIProxy<In, Out> {
    var input: In?

    val completableDeferred: CompletableDeferred<FSMResult<Out>>

    fun complete(data: Out)
    fun error(error: Throwable)
    fun back()
}

interface PlatformDependencies {
    fun flowEnd()
}

interface PlatformFSMOperations {
    suspend fun <Out> showUI(proxy: UIProxy<*, Out>): Deferred<FSMResult<Out>>
    fun createChildOperations(): PlatformFSMOperations
}
