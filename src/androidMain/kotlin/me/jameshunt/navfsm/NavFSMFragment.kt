package me.jameshunt.navfsm

import androidx.fragment.app.Fragment
import kotlinx.coroutines.Deferred

sealed class AndroidUIInput<out Input> {
    data class NewData<Input>(val data: Input): AndroidUIInput<Input>()
    object ResumeSavedState: AndroidUIInput<Nothing>()
}

abstract class NavFSMFragment<Input, Output> : Fragment() {

    var proxy: UIProxy<Input, Output>? = null

    private var newInput = false

    fun flowForResultAsync(): Deferred<FSMResult<Output>> {
        newInput = true
        return proxy!!.completableDeferred
    }

    fun getAndConsumeInputData(): AndroidUIInput<Input> {
        return when(newInput) {
            true -> AndroidUIInput.NewData(proxy!!.input) as AndroidUIInput<Input>
            false -> AndroidUIInput.ResumeSavedState
        }.also { newInput = false }
    }

    fun complete(output: Output) {
        this.proxy!!.complete(output)
    }

    fun error(error: Throwable) {
        this.proxy!!.error(error)
    }
}