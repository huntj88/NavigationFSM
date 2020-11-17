package me.jameshunt.example

import kotlinx.coroutines.CompletableDeferred
import me.jameshunt.navfsm.*

fun PlatformFSMOperations.expose(): Unit = TODO()
fun IosFSMOperations.expose(): Unit = TODO()
fun ExposedIosFSMOperations.expose(): Unit = TODO()
fun ExposedIosFSMOperations.ExposedResult.expose(): Unit = TODO()
fun ExposedIosFSMOperations.Complete.expose(): Unit = TODO()
fun ExposedIosFSMOperations.Error.expose(): Unit = TODO()
fun ExposedIosFSMOperations.Back.expose(): Unit = TODO()
fun PlatformDependencies.expose(): Unit = TODO()
fun FSMManager.expose(): Unit = TODO()


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