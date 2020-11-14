package me.jameshunt.navfsm

import kotlinx.coroutines.CompletableDeferred

interface UIProxy<In, Out> {
    val type: Type
    var input: In?

    val completableDeferred: CompletableDeferred<FSMResult<Out>>

    fun complete(data: Out)
    fun error(error: Throwable)
    fun back()

    enum class Type {
        Screen,
        Dialog
    }
}

interface PlatformDependencies

interface PlatformOperations {
    suspend fun <In, Out> showUI(ui: UIProxy<In, Out>, input: In): FSMResult<Out>
}
