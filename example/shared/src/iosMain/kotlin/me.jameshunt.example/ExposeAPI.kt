package me.jameshunt.example

import kotlinx.coroutines.CompletableDeferred
import me.jameshunt.navfsm.*
import kotlin.reflect.KClass

fun PlatformFSMOperations.expose(): Unit = TODO()
fun IosFSMOperations.expose(): Unit = TODO()
fun ExposedIosFSMOperations.expose(): Unit = TODO()
fun ExposedIosFSMOperations.ExposedResult.expose(): Unit = TODO()
fun ExposedIosFSMOperations.Complete.expose(): Unit = TODO()
fun ExposedIosFSMOperations.Error.expose(): Unit = TODO()
fun ExposedIosFSMOperations.Back.expose(): Unit = TODO()
fun PlatformDependencies.expose(): Unit = TODO()
fun FSMManager.expose(): Unit = TODO()
fun LoginNavFSM.Credentials.expose(): Unit = TODO()

fun loginUIProxyKClass(): KClass<LoginUIProxy> = LoginUIProxy::class
fun errorUIProxyKClass(): KClass<ErrorUIProxy> = ErrorUIProxy::class

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
    current: CompletableDeferred<ExposedIosFSMOperations.ExposedResult>
): CompletableDeferred<ExposedIosFSMOperations.ExposedResult> {
    return when (current.isActive) {
        true -> current
        false -> CompletableDeferred()
    }
}

fun finishedDeferred(): CompletableDeferred<ExposedIosFSMOperations.ExposedResult> {
    return CompletableDeferred(value = ExposedIosFSMOperations.Back())
}