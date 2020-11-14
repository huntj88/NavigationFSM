package me.jameshunt.navfsm

import androidx.fragment.app.FragmentManager

class AndroidDependencies(val fragmentManager: FragmentManager) : PlatformDependencies

class AndroidOperations(private val viewId: Int, private val getFragmentManager: () -> FragmentManager) :
    PlatformOperations {

    override suspend fun <In, Out> showUI(proxy: UIProxy<In, Out>, input: In): FSMResult<Out> {
        proxy as AndroidUIProxy
        val deferred = when (proxy.type) {
            UIProxy.Type.Screen -> showFragment(proxy).flowForResultAsync()
            UIProxy.Type.Dialog -> showDialog(proxy).flowForResultAsync()
        }

        return deferred.await() as FSMResult<Out>
    }

    private fun showFragment(proxy: AndroidUIProxy): NavFSMFragment<*, *> {
        val fragment = proxy.fragmentInstance()
        proxy.bind(fragment)
        getFragmentManager()
            .beginTransaction()
            .replace(viewId, fragment)
            .commit()

        return fragment
    }

    private fun showDialog(proxy: AndroidUIProxy): NavFSMDialogFragment<*, *> {
        val dialog = proxy.dialogInstance()
        proxy.bind(dialog)
        dialog.show(getFragmentManager(), null)
        return dialog
    }
}

interface AndroidUIProxy {
    fun fragmentInstance(): NavFSMFragment<*, *>
    fun dialogInstance(): NavFSMDialogFragment<*, *>
    fun bind(fragment: NavFSMFragment<*, *>)
    fun bind(dialog: NavFSMDialogFragment<*, *>)
}