package me.jameshunt.navfsm

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CompletableDeferred
import me.jameshunt.navfsm.test.ErrorUIProxy
import java.lang.ref.WeakReference


class ErrorDialog: NavFSMDialogFragment<String, Unit>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return View(context).apply { setBackgroundColor(Color.RED) }
    }
}


class ErrorUIProxyImpl : ErrorUIProxy, AndroidUIProxy {

    override var completableDeferred: CompletableDeferred<FSMResult<Unit>> = CompletableDeferred(FSMResult.Back)
        get() {
            field = if (field.isActive) field else CompletableDeferred()
            return field
        }

    internal var dialog: WeakReference<NavFSMDialogFragment<*, *>>? = null
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

    private fun restoreState(dialog: NavFSMDialogFragment<*, *>) {
        this.state?.let {
            // Can't set initial state if already added
            if (!dialog.isAdded) {
                dialog.setInitialSavedState(this.state)
            }
        }
    }

    override fun bind(fragment: NavFSMFragment<*, *>) = error("will not be implemented")
    override fun fragmentInstance(): NavFSMFragment<*, *> = error("will not be implemented")

    override fun dialogInstance(): NavFSMDialogFragment<*, *> = ErrorDialog()
    override fun bind(dialog: NavFSMDialogFragment<*, *>) {
        dialog as ErrorDialog
        this.restoreState(dialog)
        this.dialog = WeakReference(dialog)
        dialog.proxy = this
    }
}