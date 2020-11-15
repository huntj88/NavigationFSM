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
        var nextState: StateAfter = onShowForm()
        while (nextState !is Done && nextState !is Back) {
            val currentState = nextState
            nextState = when (currentState) {
                is ShowForm -> onShowForm()
                is AttemptLogin -> onAttemptLogin(currentState.credentials)
                is ShowError -> onShowError(currentState.message)
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

    suspend fun onShowForm(): StateAfterShowForm
    suspend fun onAttemptLogin(credentials: Credentials): StateAfterAttemptLogin
    suspend fun onShowError(message: String): StateAfterShowError

    fun toShowForm() = ShowForm
    fun toAttemptLogin(credentials: Credentials) = AttemptLogin(credentials)
    fun toShowError(message: String) = ShowError(message)
    fun toDone() = Done(Unit)
    fun toBack() = Back

}

class LoginNavFSMImpl : LoginNavFSM, FSM<Unit, Unit> {

    private val loginUIProxy = FSMManager.proxy<LoginUIProxy, Unit, Credentials>()
    private val errorDialogProxy = FSMManager.proxy<ErrorUIProxy, String, Unit>()

    override suspend fun onShowForm(): StateAfterShowForm {
        return flow(proxy = loginUIProxy, input = Unit).onResult(
            onComplete = { toAttemptLogin(it) },
            onBack = { toBack() }
        )
    }

    override suspend fun onAttemptLogin(credentials: Credentials): StateAfterAttemptLogin {
        return when (credentials.username == "wow" && credentials.password == "not wow") {
            true -> toDone()
            false -> toShowError("invalid credentials")
        }
    }

    override suspend fun onShowError(message: String): StateAfterShowError {
        return flow(proxy = errorDialogProxy, input = message).onResult(
            onComplete = { toShowForm() },
            onBack = { toShowForm() }
        )
    }
}

interface LoginUIProxy : UIProxy<Unit, Credentials>
interface ErrorUIProxy : UIProxy<String, Unit>