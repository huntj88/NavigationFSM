package me.jameshunt.example

import kotlinx.coroutines.CompletableDeferred
import me.jameshunt.navfsm.*
import kotlin.reflect.KClass

fun PlatformFSMOperations.expose(): Unit = TODO()
fun PlatformDependencies.expose(): Unit = TODO()
fun FSMManager.expose(): Unit = TODO()
fun LoginNavFSM.Credentials.expose(): Unit = TODO()

fun loginUIProxyKClass(): KClass<LoginUIProxy> = LoginUIProxy::class
fun errorUIProxyKClass(): KClass<ErrorUIProxy> = ErrorUIProxy::class

fun complete(output: Any?): FSMResult.Complete<*> = FSMResult.Complete(output)
fun error(error: Throwable): FSMResult.Error = FSMResult.Error(error)
fun back(): FSMResult.Back = FSMResult.Back

data class UIRegistryEntry(
    val kClass: KClass<UIProxy<*, *>>,
    val newInstance: () -> UIProxy<*, *>
)

fun iosConfigure(
    uiRegistry: List<UIRegistryEntry>,
    getInitialFlow: () -> FSM<Unit, Unit>
) = me.jameshunt.navfsm.iosConfigure(
    uiRegistry = uiRegistry.map { it.kClass to it.newInstance }.toMap(),
    getInitialFlow = getInitialFlow
)

fun activeDeferred(
    current: CompletableDeferred<FSMResult<*>>
): CompletableDeferred<FSMResult<*>> {
    return when (current.isActive) {
        true -> current
        false -> CompletableDeferred()
    }
}

fun finishedDeferred(): CompletableDeferred<FSMResult<*>> {
    return CompletableDeferred(value = FSMResult.Back)
}