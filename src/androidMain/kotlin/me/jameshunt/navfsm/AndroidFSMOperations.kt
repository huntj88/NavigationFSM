package me.jameshunt.navfsm

import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.Deferred
import java.lang.ref.WeakReference

class AndroidDependencies(
    val fragmentManager: FragmentManager,
    private val flowFinished: () -> Unit
) : PlatformDependencies {

    fun resume() {
        FSMManager.root.findGroup().getFSMLeafs().forEach {
            it.platformFSMOperations.android().resume()
        }
//        FSMManager.root.walkTreeForOperation { it.platformFSMOperations.android().resume() }
    }

    fun back() {
//        FSMManager.root.findGroup().getFSMLeafs().forEach {
//            it.platformFSMOperations.android().back() // todo: delegate to the "active" FSM
//        }
//        FSMManager.root.walkTreeForOperation { it.platformFSMOperations.android().back() }
    }

    override fun flowEnd() {
        flowFinished()
    }
}

fun PlatformFSMOperations.android(): AndroidFSMOperations = this as AndroidFSMOperations
data class AndroidFSMOperations(private val viewId: Int, private val fragmentContainerTag: String) : PlatformFSMOperations {

    var mostRecentFragmentProxy: FragmentProxy? = null
    var mostRecentDialogProxy: DialogProxy? = null

    private val fragmentManager: FragmentManager
        get() = (FSMManager.platformDependencies as AndroidDependencies).fragmentManager.findFragmentByTag(fragmentContainerTag)!!.childFragmentManager

    override fun createChildOperations(): PlatformFSMOperations = this.copy()

    fun resume() {
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

    override suspend fun <Out> showUI(proxy: UIProxy<*, Out>): Deferred<FSMResult<Out>> {
        val deferred = when (proxy) {
            is FragmentProxy -> showFragment(proxy).flowForResultAsync()
            is DialogProxy -> showDialog(proxy).flowForResultAsync()
            else -> TODO("$proxy")
        }

        return deferred as Deferred<FSMResult<Out>>
    }

    private fun showFragment(proxy: FragmentProxy): NavFSMFragment<*, *, *> {
        mostRecentFragmentProxy = proxy
        mostRecentDialogProxy = null
        val fragment = proxy.fragmentOrNew()
        proxy.bind(fragment)
        fragmentManager
            .beginTransaction()
            .replace(viewId, fragment, proxy.tag)
            .commit()

        return fragment
    }

    private fun showDialog(proxy: DialogProxy): NavFSMDialogFragment<*, *, *> {
        mostRecentDialogProxy = proxy
        val dialog = proxy.dialogOrNew()
        proxy.bind(dialog)
        dialog.show(fragmentManager, proxy.tag)
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
