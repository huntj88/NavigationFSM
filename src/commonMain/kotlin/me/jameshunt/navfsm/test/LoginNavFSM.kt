package me.jameshunt.navfsm.test

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import me.jameshunt.navfsm.FSM
import me.jameshunt.navfsm.FSMResult
import me.jameshunt.navfsm.UIProxy
import me.jameshunt.navfsm.flow

interface LoginNavFSM: FSM<Unit, Unit> {
    data class ProvidedCredentials(val username: String, val password: String)

    sealed class LoginFlowState {
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
                var nextState: LoginFlowState.StateAfter = onShowForm()
                while (nextState !is LoginFlowState.Done) {
                    val currentState = nextState
                    nextState = when (currentState) {
                        is LoginFlowState.ShowForm -> onShowForm()
                        is LoginFlowState.AttemptLogin -> onAttemptLogin(currentState.credentials)
                        is LoginFlowState.ShowError -> onShowError(currentState.message)
                        else -> throw IllegalStateException()
                    }
                }

                val flowOutput = nextState
                    .let { it as? LoginFlowState.Done }?.output
                    ?: throw IllegalStateException()

                FSMResult.Complete(flowOutput)
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

    suspend fun onShowForm(): LoginFlowState.StateAfterShowForm
    suspend fun onAttemptLogin(credentials: ProvidedCredentials): LoginFlowState.StateAfterAttemptLogin
    suspend fun onShowError(message: String): LoginFlowState.StateAfterShowError

    fun toShowForm() = LoginFlowState.ShowForm
    fun toAttemptLogin(credentials: ProvidedCredentials) = LoginFlowState.AttemptLogin(credentials)
    fun toShowError(message: String) = LoginFlowState.ShowError(message)
    fun toDone() = LoginFlowState.Done(Unit)
    fun toBack() = LoginFlowState.Back

}

class LoginNavFSMImpl: LoginNavFSM, FSM<Unit, Unit> {
    override suspend fun onShowForm(): LoginNavFSM.LoginFlowState.StateAfterShowForm {
//        return when(val result = flow(LoginFlowImpl(), Unit)) {
//            is FlowResult.Complete -> toAttemptLogin(LoginFlow.ProvidedCredentials("", ""))
//            is FlowResult.Back -> toBack()
//            is FlowResult.Error -> TODO()
//        }

//        delay(5000)
        val result = flow<Unit, LoginNavFSM.ProvidedCredentials>(
            ui = LoginUIProxy::class,
            input = Unit
        )

        println(result)
        TODO()
    }

    override suspend fun onAttemptLogin(credentials: LoginNavFSM.ProvidedCredentials): LoginNavFSM.LoginFlowState.StateAfterAttemptLogin {
        return when(val result = flow(LoginNavFSMImpl(), Unit)) {
            is FSMResult.Complete -> toDone()
            is FSMResult.Back -> TODO()
            is FSMResult.Error -> toShowError(result.error.stackTraceToString())
        }
    }

    override suspend fun onShowError(message: String): LoginNavFSM.LoginFlowState.StateAfterShowError {
        return when(val result = flow(LoginNavFSMImpl(), Unit)) {
            is FSMResult.Complete -> toShowForm()
            is FSMResult.Back -> toShowForm()
            is FSMResult.Error -> TODO()
        }
    }
}

interface LoginUIProxy: UIProxy<Unit, LoginNavFSM.ProvidedCredentials> {
    override val type: UIProxy.Type
        get() = UIProxy.Type.Screen
}