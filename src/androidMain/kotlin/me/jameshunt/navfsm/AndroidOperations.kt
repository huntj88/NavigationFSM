package me.jameshunt.navfsm

import androidx.fragment.app.FragmentManager
import java.lang.ref.WeakReference

class AndroidDependencies(val fragmentManager: FragmentManager) : PlatformDependencies

class AndroidOperations(
    private val viewId: Int,
    private val getFragmentManager: () -> FragmentManager
) : PlatformOperations {

    var currentlyVisibleFragmentProxy: FragmentProxy? = null
    var currentlyVisibleDialogProxy: DialogProxy? = null

    override fun resume() {
        val fragmentManager = getFragmentManager()
        if (fragmentManager.fragments.size > 0) {
            fragmentManager
                .beginTransaction()
                .also { transaction ->
                    fragmentManager.fragments.forEach {
                        transaction.remove(it)
                    }
                }
                .commitNowAllowingStateLoss()
        }

        currentlyVisibleFragmentProxy?.let { showFragment(it) }
        currentlyVisibleDialogProxy?.let { showDialog(it) }
    }

    override suspend fun <In, Out> showUI(proxy: UIProxy<In, Out>, input: In): FSMResult<Out> {
        val deferred = when (proxy.type) {
            UIProxy.Type.Screen -> showFragment(proxy as FragmentProxy).flowForResultAsync()
            UIProxy.Type.Dialog -> showDialog(proxy as DialogProxy).flowForResultAsync()
        }

        return deferred.await() as FSMResult<Out>
    }

    private fun showFragment(proxy: FragmentProxy): NavFSMFragment<*, *> {
        currentlyVisibleFragmentProxy = proxy
        val fragment = proxy.fragmentOrNew()
        proxy.bind(fragment)
        getFragmentManager()
            .beginTransaction()
            .replace(viewId, fragment)
            .commit()

        return fragment
    }

    private fun showDialog(proxy: DialogProxy): NavFSMDialogFragment<*, *> {
        currentlyVisibleDialogProxy = proxy
        val dialog = proxy.dialogOrNew()
        proxy.bind(dialog)
        dialog.show(getFragmentManager(), null)
        return dialog
    }
}

interface FragmentProxy {
    val fragment: WeakReference<NavFSMFragment<*, *>>?
    fun newFragmentInstance(): NavFSMFragment<*, *>
    fun bind(fragment: NavFSMFragment<*, *>)
}

fun FragmentProxy.fragmentOrNew(): NavFSMFragment<*, *> = fragment?.get() ?: newFragmentInstance()
fun DialogProxy.dialogOrNew(): NavFSMDialogFragment<*, *> = dialog?.get() ?: newDialogInstance()

interface DialogProxy {
    val dialog: WeakReference<NavFSMDialogFragment<*, *>>?
    fun newDialogInstance(): NavFSMDialogFragment<*, *>
    fun bind(dialog: NavFSMDialogFragment<*, *>)
}