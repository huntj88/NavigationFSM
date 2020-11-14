package me.jameshunt.example

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import me.jameshunt.navfsm.*
import me.jameshunt.navfsm.test.ErrorUIProxy
import me.jameshunt.navfsm.test.LoginNavFSMImpl
import me.jameshunt.navfsm.test.LoginUIProxy
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        layoutInflater.inflate(R.layout.fullscreen_fragment_container, navFSMContainer, true)

        FSMManager.init(
            platformDependencies = AndroidDependencies(supportFragmentManager),
            scope = (application as App).fsmNavScope,
            getInitialFlow = { LoginNavFSMImpl() },
            uiRegistry = mapOf(
                LoginUIProxy::class as KClass<UIProxy<*, *>> to { LoginUIProxyImpl() },
                ErrorUIProxy::class as KClass<UIProxy<*, *>> to { ErrorUIProxyImpl() }
            ),
            platformOperations = AndroidOperations(R.id.fragment_container) {
                (FSMManager.platformDependencies as AndroidDependencies).fragmentManager
            }
        )
    }
}

class App : Application() {
    private val job = Job()
    val fsmNavScope = CoroutineScope(job + Dispatchers.Main)
}
