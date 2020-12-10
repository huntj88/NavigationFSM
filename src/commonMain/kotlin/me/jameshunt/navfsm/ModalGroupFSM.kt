package me.jameshunt.navfsm

import kotlinx.coroutines.CompletableDeferred

class ModalGroupFSM: FSM<Unit, Unit>, FSMGroup {

    val modalContainer = FSMManager.proxy<ModalGroupProxy>()

    override suspend fun run(input: Unit): FSMResult<Unit> {
        TODO("Not yet implemented")
    }
}

class ModalGroupProxy: UIProxy<Unit, Unit> {

    override var completableDeferred: CompletableDeferred<FSMResult<Unit>> =
        CompletableDeferred(FSMResult.Back)
        get() {
            field = if (field.isActive) field else CompletableDeferred()
            return field
        }
    override var input: Unit? = null

    override fun complete(data: Unit) {
        TODO("Not yet implemented")
    }

    override fun error(error: Throwable) {
        TODO("Not yet implemented")
    }

    override fun back() {
        TODO("Not yet implemented")
    }


}