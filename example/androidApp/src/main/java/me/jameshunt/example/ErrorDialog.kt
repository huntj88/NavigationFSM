package me.jameshunt.example

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CompletableDeferred
import me.jameshunt.navfsm.AndroidUIInput
import me.jameshunt.navfsm.DialogProxy
import me.jameshunt.navfsm.FSMResult
import me.jameshunt.navfsm.NavFSMDialogFragment
import me.jameshunt.navfsm.test.ErrorUIProxy
import java.lang.ref.WeakReference


class ErrorDialog: NavFSMDialogFragment<ErrorUIProxy, String, Unit>() {

    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        counter = when(getAndConsumeInputData()) {
            is AndroidUIInput.NewData -> 0
            is AndroidUIInput.ResumeSavedState -> savedInstanceState
                ?.getInt("counter")
                ?.plus(1)
                ?: 0
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("counter", counter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TextView(context).apply {
            textSize = 32f
            text = counter.toString()
            setPadding(150, 150, 150, 150)
            setBackgroundColor(Color.RED)
            setOnClickListener {
                complete(Unit)
            }
        }
    }
}

// generated
class ErrorUIProxyImpl : ErrorUIProxy, DialogProxy {
    override val tag: String = java.util.UUID.randomUUID().toString()

    override var completableDeferred: CompletableDeferred<FSMResult<Unit>> = CompletableDeferred(FSMResult.Back)
        get() {
            field = if (field.isActive) field else CompletableDeferred()
            return field
        }

    override var dialog: WeakReference<NavFSMDialogFragment<*, *, *>>? = null
    private var state: Fragment.SavedState? = null
    override var input: String? = null

    override fun complete(data: Unit) {
        saveState()
        completableDeferred.complete(FSMResult.Complete(data))
    }

    override fun back() {
        saveState()
        completableDeferred.complete(FSMResult.Back)
    }

    override fun error(error: Throwable) {
        state = null
        completableDeferred.complete(FSMResult.Error(error))
    }

    internal fun saveState() {
        this.dialog?.get()?.let {
            // We can't save the state of a Fragment that isn't added to a FragmentManager.
            if (it.isAdded) {
                this.state = it.fragmentManager?.saveFragmentInstanceState(it)
            }
        }
    }

    private fun restoreState(dialog: NavFSMDialogFragment<*, *, *>) {
        this.state?.let {
            // Can't set initial state if already added
            if (!dialog.isAdded) {
                dialog.setInitialSavedState(this.state)
            }
        }
    }

    override fun newDialogInstance(): NavFSMDialogFragment<*, *, *> = ErrorDialog()
    override fun bind(dialog: NavFSMDialogFragment<*, *, *>) {
        dialog as ErrorDialog
        this.restoreState(dialog)
        this.dialog = WeakReference(dialog)
        dialog.proxy = this
    }
}