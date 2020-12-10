package me.jameshunt.navfsm

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class SimpleGroupFSM(private val initialFlow: FSM<Unit, Unit>) : FSM<Unit, Unit>, FSMGroup {

    val proxy = FSMManager.proxy<SimpleGroupProxy>()

    override suspend fun run(input: Unit): FSMResult<Unit> {
        val group: FSMTreeNode = FSMManager.root.findGroup()

        group.platformFSMOperations.showUI(proxy).await()
        delay(1000)

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
