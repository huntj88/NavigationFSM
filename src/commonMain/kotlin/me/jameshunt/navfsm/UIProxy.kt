package me.jameshunt.navfsm

import kotlinx.coroutines.CompletableDeferred

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
    suspend fun <In, Out> showUI(proxy: UIProxy<In, Out>, input: In): FSMResult<Out>
    fun duplicate(): PlatformFSMOperations
}
