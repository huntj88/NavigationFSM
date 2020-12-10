package me.jameshunt.navfsm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import me.jameshunt.android.R
import java.lang.ref.WeakReference
import java.util.*

class SimpleGroupFragment: NavFSMFragment<SimpleGroupProxy, Unit, Unit>() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.simple_group_container, container, false)
}

// generated
class SimpleGroupProxyImpl : SimpleGroupProxy, FragmentProxy {
    override val tag: String = UUID.randomUUID().toString()

    override var completableDeferred: CompletableDeferred<FSMResult<Unit>> =
        CompletableDeferred(FSMResult.Back)
        get() {
            field = if (field.isActive) field else CompletableDeferred()
            return field
        }

    override var fragment: WeakReference<NavFSMFragment<*, *, *>>? = null
    private var state: Fragment.SavedState? = null
    override var input: Unit? = Unit

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
        this.fragment?.get()?.let {
            // We can't save the state of a Fragment that isn't added to a FragmentManager.
            if (it.isAdded) {
                this.state = it.fragmentManager?.saveFragmentInstanceState(it)
            }
        }
    }

    private fun restoreState(fragment: NavFSMFragment<*, *, *>) {
        this.state?.let {
            // Can't set initial state if already added
            if (!fragment.isAdded) {
                fragment.setInitialSavedState(this.state)
            }
        }
    }

    override fun newFragmentInstance(): NavFSMFragment<*, *, *> = SimpleGroupFragment()
    override fun bind(fragment: NavFSMFragment<*, *, *>) {
        fragment as SimpleGroupFragment
        this.restoreState(fragment)
        this.fragment = WeakReference(fragment)
        fragment.setProxy(this)
    }
}

class SingleGroupAndroidOperations: PlatformFSMOperations {
    var groupFragmentProxy: FragmentProxy? = null

    private val fragmentManager: FragmentManager
        get() = (FSMManager.platformDependencies as AndroidDependencies).fragmentManager

    override suspend fun <Out> showUI(proxy: UIProxy<*, Out>): Deferred<FSMResult<Out>> {
        return showFragment(proxy = proxy as FragmentProxy)
            .flowForResultAsync() as Deferred<FSMResult<Out>>
    }

    override fun resume() {
        val proxy = groupFragmentProxy!!
        val fragment = fragmentManager.findFragmentByTag(proxy.tag) as? NavFSMFragment<*, *, *>

        fragment
            ?.let { proxy.bind(it) }
            ?: showFragment(proxy = groupFragmentProxy as FragmentProxy)
    }

    override fun createChildOperations(): PlatformFSMOperations {
        return AndroidFSMOperations(
            viewId = R.id.simple_group_container,
            fragmentContainerTag = groupFragmentProxy!!.tag
        )
    }

    private fun showFragment(proxy: FragmentProxy): NavFSMFragment<*, *, *> {
        groupFragmentProxy = proxy
        val fragment = proxy.fragmentOrNew()
        proxy.bind(fragment)
        fragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, proxy.tag)
            .commit()

        return fragment
    }
}