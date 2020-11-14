package me.jameshunt.navfsm

import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CompletableDeferred
import me.jameshunt.navfsm.test.LoginNavFSM
import me.jameshunt.navfsm.test.LoginUIProxy
import java.lang.ref.WeakReference
import java.util.concurrent.Executors

class LoginFragment : NavFSMFragment<Unit, LoginNavFSM.ProvidedCredentials>() {

    init {
        Executors.newSingleThreadExecutor().execute {
            Thread.sleep(1000)

            Handler(requireActivity().mainLooper).post {
                complete(LoginNavFSM.ProvidedCredentials("wow", "sup"))
            }
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
            it as Fragment
            // We can't save the state of a Fragment that isn't added to a FragmentManager.
            if (it.isAdded) {
                this.state = it.fragmentManager?.saveFragmentInstanceState(it)
            }
        }
    }

    private fun restoreState(fragment: NavFSMFragment<*, *>) {
        this.state?.let {
            fragment as Fragment
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