package me.jameshunt.navfsm.test

import me.jameshunt.navfsm.*
import me.jameshunt.navfsm.test.LoginNavFSM.*
import me.jameshunt.navfsm.test.LoginNavFSM.LoginFlowState.*

// generated
interface LoginNavFSM : FSM<Unit, Unit> {
    data class Credentials(val username: String, val password: String)

    sealed class LoginFlowState : State {
        interface StateAfter
        interface StateAfterShowForm : StateAfter
        interface StateAfterAttemptLogin : StateAfter
        interface StateAfterShowError : StateAfter

        object ShowForm : LoginFlowState(), StateAfterShowError
        data class AttemptLogin(val credentials: Credentials) : LoginFlowState(), StateAfterShowForm
        data class ShowError(val message: String) : LoginFlowState(), StateAfterAttemptLogin

        object Back : LoginFlowState(), StateAfterShowForm
        data class Done(val output: Unit) : LoginFlowState(), StateAfterAttemptLogin
    }

    override suspend fun run(input: Unit): FSMResult<Unit> = try {
        var nextState: StateAfter = ShowForm.handle()
        while (nextState !is Done && nextState !is Back) {
            val currentState = nextState
            nextState = when (currentState) {
                is ShowForm -> currentState.handle()
                is AttemptLogin -> currentState.handle()
                is ShowError -> currentState.handle()
                else -> throw IllegalStateException()
            }
        }

        when (nextState) {
            is Done -> FSMResult.Complete(nextState.output)
            is Back -> FSMResult.Back
            else -> throw IllegalStateException()
        }
    } catch (t: Throwable) {
        FSMResult.Error(t)
    }

    suspend fun ShowForm.handle(): StateAfterShowForm
    suspend fun AttemptLogin.handle(): StateAfterAttemptLogin
    suspend fun ShowError.handle(): StateAfterShowError

    fun ShowError.toShowForm() = ShowForm
    fun ShowForm.toAttemptLogin(credentials: Credentials) = AttemptLogin(credentials)

    fun AttemptLogin.toShowError(message: String) = ShowError(message)
    fun AttemptLogin.toDone() = Done(Unit)
    fun ShowForm.toBack() = Back

}

class BlahNavFSM : FSM<String, String> {
    private val errorDialogProxy = FSMManager.proxy<ErrorUIProxy, String, Unit>()

    override suspend fun run(input: String): FSMResult<String> {
        flow(proxy = errorDialogProxy, input = input)
        return FSMResult.Complete("complete")
    }
}

class LoginNavFSMImpl : LoginNavFSM {

    private val loginUIProxy = FSMManager.proxy<LoginUIProxy, Unit, Credentials>()
    private val errorDialogProxy = FSMManager.proxy<ErrorUIProxy, String, Unit>()

    override suspend fun ShowForm.handle(): StateAfterShowForm {
        return flow(proxy = loginUIProxy, input = Unit).onResult(
            onComplete = { toAttemptLogin(it) },
            onBack = { toBack() }
        )
    }

    override suspend fun AttemptLogin.handle(): StateAfterAttemptLogin {
        return when (credentials.username == "wow" && credentials.password == "not wow") {
            true -> toDone()
            false -> toShowError("invalid credentials")
        }
    }

    override suspend fun ShowError.handle(): StateAfterShowError {
        return flow(flow = BlahNavFSM(), input = "input")
//        return flow(proxy = errorDialogProxy, input = message)
            .onResult(
                onComplete = { toShowForm() },
                onBack = { toShowForm() }
            )
    }
}

interface LoginUIProxy : UIProxy<Unit, Credentials>
interface ErrorUIProxy : UIProxy<String, Unit>