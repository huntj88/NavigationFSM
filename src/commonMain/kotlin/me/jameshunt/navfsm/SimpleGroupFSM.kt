package me.jameshunt.navfsm

import kotlinx.coroutines.delay

class SimpleGroupFSM(private val initialFlow: FSM<Unit, Unit>) : FSM<Unit, Unit>, FSMGroup {

    private val proxy = FSMManager.proxy<SimpleGroupProxy>()

    override suspend fun run(input: Unit): FSMResult<Unit> {
        val group: FSMTreeNode = FSMManager.root.findGroup()

        group.platformFSMOperations.showUI(proxy)
            .also { proxy.complete(Unit) }
            .await()

        delay(1) // todo

        // todo, provision initial flow with new view stuff
        return this.flow(initialFlow, Unit)
            .also {
                (it as? FSMResult.Error)?.let { throw it.error }
            }
    }

    fun getFSMOperationsForChildren() {

    }
}

interface SimpleGroupProxy : UIProxy<Unit, Unit>
