package me.jameshunt.navfsm

import kotlinx.coroutines.GlobalScope

fun iosConfigure(uiRegistry: UIRegistry, getInitialFlow: () -> FSM<Unit, Unit>) {
    FSMManager.config(
        scope = GlobalScope, // gross?
        uiRegistry = uiRegistry,
        getInitialFlow = getInitialFlow
    )
}