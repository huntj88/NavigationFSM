package me.jameshunt.navfsm.test

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import me.jameshunt.navfsm.*
import me.jameshunt.navfsm.test.LoginNavFSM.*
import me.jameshunt.navfsm.test.LoginNavFSM.LoginFlowState.*

interface LoginNavFSM: FSM<Unit, Unit> {
    data class ProvidedCredentials(val username: String, val password: String)

    sealed class LoginFlowState: State {
        interface StateAfter
        interface StateAfterShowForm : StateAfter
        interface StateAfterAttemptLogin : StateAfter
        interface StateAfterShowError : StateAfter

        object ShowForm : LoginFlowState(), StateAfterShowError
        data class AttemptLogin(val credentials: ProvidedCredentials) : LoginFlowState(),
            StateAfterShowForm
        data class ShowError(val message: String) : LoginFlowState(), StateAfterAttemptLogin

        object Back : LoginFlowState(), StateAfterShowForm
        data class Done(val output: Unit) : LoginFlowState(), StateAfterAttemptLogin
    }

    override suspend fun run(input: Unit): FSMResult<Unit> = coroutineScope {
        val normalFlow = async {
            try {
                var nextState: StateAfter = onShowForm()
                while (nextState !is Done) {
                    val currentState = nextState
                    nextState = when (currentState) {
                        is ShowForm -> onShowForm()
                        is AttemptLogin -> onAttemptLogin(currentState.credentials)
                        is ShowError -> onShowError(currentState.message)
                        else -> throw IllegalStateException()
                    }
                }

                FSMResult.Complete(nextState.output)
            } catch (t: Throwable) {
                FSMResult.Error(t)
            }
        }

//        val backFlow = async {
//            delay(50000)
//            FSMResult.Back
//        }

        select<FSMResult<Unit>> {
            normalFlow.onAwait { it }
//            backFlow.onAwait { it }
        }
    }

    suspend fun onShowForm(): StateAfterShowForm
    suspend fun onAttemptLogin(credentials: ProvidedCredentials): StateAfterAttemptLogin
    suspend fun onShowError(message: String): StateAfterShowError

    fun toShowForm() = ShowForm
    fun toAttemptLogin(credentials: ProvidedCredentials) = AttemptLogin(credentials)
    fun toShowError(message: String) = ShowError(message)
    fun toDone() = Done(Unit)
    fun toBack() = Back

}

class LoginNavFSMImpl: LoginNavFSM, FSM<Unit, Unit> {

    private val loginUIProxy = FSMManager.uiRegistry.getProxyInstance<LoginUIProxy, Unit, ProvidedCredentials>()

    override suspend fun onShowForm(): StateAfterShowForm {
        return flow(proxy = loginUIProxy, input = Unit).onResult(
            onComplete = { AttemptLogin(it) }
        )
    }

    override suspend fun onAttemptLogin(credentials: ProvidedCredentials): StateAfterAttemptLogin {
        return when (credentials.username == "wow" && credentials.password == "wow") {
            true -> toDone()
            false -> toShowError("invalid credentials")
        }
    }

    override suspend fun onShowError(message: String): StateAfterShowError {
        return when(val result = flow(LoginNavFSMImpl(), Unit)) {
            is FSMResult.Complete -> toShowForm()
            is FSMResult.Back -> toShowForm()
            is FSMResult.Error -> TODO()
        }
    }
}

interface LoginUIProxy: UIProxy<Unit, ProvidedCredentials> {
    override val type: UIProxy.Type
        get() = UIProxy.Type.Screen
}