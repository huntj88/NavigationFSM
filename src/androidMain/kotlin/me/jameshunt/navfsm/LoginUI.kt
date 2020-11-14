package me.jameshunt.navfsm

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CompletableDeferred
import me.jameshunt.navfsm.test.LoginNavFSM
import me.jameshunt.navfsm.test.LoginUIProxy
import java.lang.ref.WeakReference

class LoginFragment : NavFSMFragment<Unit, LoginNavFSM.ProvidedCredentials>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = View(context).apply {
        setBackgroundColor(Color.BLUE)
        setOnClickListener {
            val output = LoginNavFSM.ProvidedCredentials("wow", "wow")
            complete(output)
        }
    }
}

class LoginUIProxyImpl : LoginUIProxy, AndroidUIProxy {

    override var completableDeferred: CompletableDeferred<FSMResult<LoginNavFSM.ProvidedCredentials>> =
        CompletableDeferred(FSMResult.Back)
        get() {
            field = if (field.isActive) field else CompletableDeferred()
            return field
        }

    internal var fragment: WeakReference<NavFSMFragment<*, *>>? = null
    private var state: Fragment.SavedState? = null
    override var input: Unit? = Unit

    override fun complete(data: LoginNavFSM.ProvidedCredentials) {
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
        this.fragment?.get()?.let {
            // We can't save the state of a Fragment that isn't added to a FragmentManager.
            if (it.isAdded) {
                this.state = it.fragmentManager?.saveFragmentInstanceState(it)
            }
        }
    }

    private fun restoreState(fragment: NavFSMFragment<*, *>) {
        this.state?.let {
            // Can't set initial state if already added
            if (!fragment.isAdded) {
                fragment.setInitialSavedState(this.state)
            }
        }
    }

    override fun fragmentInstance(): NavFSMFragment<*, *> = LoginFragment()
    override fun bind(fragment: NavFSMFragment<*, *>) {
        fragment as LoginFragment
        this.restoreState(fragment)
        this.fragment = WeakReference(fragment)
        fragment.proxy = this
    }
}