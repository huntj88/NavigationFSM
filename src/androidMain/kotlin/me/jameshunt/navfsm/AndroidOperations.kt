package me.jameshunt.navfsm

import androidx.fragment.app.FragmentManager

class AndroidDependencies(val fragmentManager: FragmentManager) : PlatformDependencies

class AndroidOperations(private val viewId: Int, private val getFragmentManager: () -> FragmentManager) :
    PlatformOperations {

    override suspend fun <In, Out> showUI(ui: UIProxy<In, Out>, input: In): FSMResult<Out> {
        return (ui as AndroidUIProxy)
            .fragmentInstance()
            .also {
                ui.bind(it)
                getFragmentManager()
                    .beginTransaction()
                    .replace(viewId, it)
                    .commit()
            }
            .flowForResultAsync()
            .await() as FSMResult<Out>
    }
}

interface AndroidUIProxy {
    fun fragmentInstance(): NavFSMFragment<*, *>
    fun bind(fragment: NavFSMFragment<*, *>)
}