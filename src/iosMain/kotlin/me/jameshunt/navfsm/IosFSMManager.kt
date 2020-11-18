package me.jameshunt.navfsm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

object IosCoroutine {
    private val job = Job()
    val scope = CoroutineScope(job + MainLoopDispatcher)
}

fun iosConfigure(uiRegistry: UIRegistry, getInitialFlow: () -> FSM<Unit, Unit>) {
    FSMManager.config(
        scope = IosCoroutine.scope,
        uiRegistry = uiRegistry,
        getInitialFlow = getInitialFlow
    )
}