package me.jameshunt.navfsm

import androidx.fragment.app.FragmentManager
import java.lang.ref.WeakReference

class AndroidDependencies(val fragmentManager: FragmentManager) : PlatformDependencies

class AndroidOperations(
    private val viewId: Int,
    private val getFragmentManager: () -> FragmentManager
) : PlatformOperations {

    var mostRecentFragmentProxy: FragmentProxy? = null
    var mostRecentDialogProxy: DialogProxy? = null

    override fun resume() {
        val fragmentManager = getFragmentManager()

        val currentFragment = fragmentManager
            .fragments
            .firstOrNull { it.tag == mostRecentFragmentProxy?.tag }

        val currentDialog = fragmentManager
            .fragments
            .firstOrNull { it.tag == mostRecentDialogProxy?.tag }

        currentFragment?.let { mostRecentFragmentProxy?.bind(it as NavFSMFragment<*, *, *>) }
        currentDialog?.let { mostRecentDialogProxy?.bind(it as NavFSMDialogFragment<*, *, *>) }
    }

    fun back() {
        mostRecentDialogProxy
            ?.let { it as UIProxy<*, *> }?.let {
                mostRecentDialogProxy?.dialog?.get()?.dismiss()
                it.back()
            }
            ?: mostRecentFragmentProxy?.let { it as UIProxy<*, *> }?.back()
            ?: throw IllegalStateException()
    }

    override suspend fun <In, Out> showUI(proxy: UIProxy<In, Out>, input: In): FSMResult<Out> {
        val deferred = when (proxy) {
            is FragmentProxy -> showFragment(proxy).flowForResultAsync()
            is DialogProxy -> showDialog(proxy).flowForResultAsync()
            else -> TODO("$proxy")
        }

        return deferred.await() as FSMResult<Out>
    }

    private fun showFragment(proxy: FragmentProxy): NavFSMFragment<*, *, *> {
        mostRecentFragmentProxy = proxy
        mostRecentDialogProxy = null
        val fragment = proxy.fragmentOrNew()
        proxy.bind(fragment)
        getFragmentManager()
            .beginTransaction()
            .replace(viewId, fragment, proxy.tag)
            .commit()

        return fragment
    }

    private fun showDialog(proxy: DialogProxy): NavFSMDialogFragment<*, *, *> {
        mostRecentDialogProxy = proxy
        val dialog = proxy.dialogOrNew()
        proxy.bind(dialog)
        dialog.show(getFragmentManager(), proxy.tag)
        return dialog
    }
}

interface FragmentProxy {
    val tag: String
    val fragment: WeakReference<NavFSMFragment<*, *, *>>?
    fun newFragmentInstance(): NavFSMFragment<*, *, *>
    fun bind(fragment: NavFSMFragment<*, *, *>)
}
fun FragmentProxy.fragmentOrNew(): NavFSMFragment<*, *, *> = fragment?.get() ?: newFragmentInstance()

interface DialogProxy {
    val tag: String
    val dialog: WeakReference<NavFSMDialogFragment<*, *, *>>?
    fun newDialogInstance(): NavFSMDialogFragment<*, *, *>
    fun bind(dialog: NavFSMDialogFragment<*, *, *>)
}
fun DialogProxy.dialogOrNew(): NavFSMDialogFragment<*, *, *> = dialog?.get() ?: newDialogInstance()
