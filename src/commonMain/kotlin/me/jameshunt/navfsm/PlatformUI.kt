package me.jameshunt.navfsm

interface PlatformUI<In, Out> {
    val type: Type

    fun onInput(input: In)
    fun complete(date: Out)
    fun error(error: Throwable)
    fun back()

    enum class Type {
        Screen,
        Dialog
    }
}

interface PlatformDependencies

interface PlatformOperations {
    suspend fun <In, Out> showUI(ui: PlatformUI<In, Out>, input: In): FSMResult<Out>
}
