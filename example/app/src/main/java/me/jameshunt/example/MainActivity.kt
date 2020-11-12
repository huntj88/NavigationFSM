package me.jameshunt.example

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import me.jameshunt.navfsm.*
import me.jameshunt.navfsm.test.LoginNavFSM
import me.jameshunt.navfsm.test.LoginNavFSMImpl
import me.jameshunt.navfsm.test.LoginUI
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        layoutInflater.inflate(R.layout.fullscreen_fragment_container, navFSMContainer, true)

        FSMManager.platformDependencies = AndroidDependencies(supportFragmentManager)
    }
}

class LoginUIImpl : LoginUI {
    val proxy: Nothing = TODO()

    override fun onInput(input: Unit) {
        TODO("Not yet implemented")
    }

    override fun complete(date: LoginNavFSM.ProvidedCredentials) {
        TODO("Not yet implemented")
    }

    override fun error(error: Throwable) {
        TODO("Not yet implemented")
    }

    override fun back() {
        TODO("Not yet implemented")
    }

}

class AndroidDependencies(val fragmentManager: FragmentManager) : PlatformDependencies

class AndroidOperations(viewId: Int, getFragmentManager: () -> FragmentManager) :
    PlatformOperations {

    override suspend fun <In, Out> showUI(ui: PlatformUI<In, Out>, input: In): FSMResult<Out> {
        TODO("Not yet implemented")
    }
}

class App : Application() {
    private val job = Job()
    private val flowScope = CoroutineScope(job + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        FSMManager.init(
            scope = flowScope,
            initialFlow = LoginNavFSMImpl(),
            uiRegistry = mapOf(
                LoginUI::class as KClass<PlatformUI<*, *>> to { LoginUIImpl() }
            ),
            platformOperations = AndroidOperations(R.id.fragment_container) {
                (FSMManager.platformDependencies as AndroidDependencies).fragmentManager
            }
        )
    }
}
