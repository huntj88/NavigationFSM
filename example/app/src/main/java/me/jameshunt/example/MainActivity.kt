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
        val platformDependencies = AndroidDependencies(supportFragmentManager)

        when (FSMManager.isInitialized()) {
            true -> FSMManager.resume(platformDependencies)
            false -> FSMManager.init(
                platformDependencies = platformDependencies,
                platformOperations = AndroidOperations(R.id.fragment_container) {
                    (FSMManager.platformDependencies as AndroidDependencies).fragmentManager
                }
            )
        }
    }

    override fun onBackPressed() {
        FSMManager.root.platformOperations.android().back()
//        super.onBackPressed()
    }
}

class App : Application(), NavFSMConfig by NavFSMConfigImpl()

interface NavFSMConfig {
    val job: Job
    val fsmNavScope: CoroutineScope
}

class NavFSMConfigImpl: NavFSMConfig {
    override val job = Job()
    override val fsmNavScope = CoroutineScope(job + Dispatchers.Main)

    init {
        FSMManager.config(
            scope = fsmNavScope,
            getInitialFlow = { LoginNavFSMImpl() },
            uiRegistry = mapOf(
                LoginUIProxy::class as KClass<UIProxy<*, *>> to { LoginUIProxyImpl() },
                ErrorUIProxy::class as KClass<UIProxy<*, *>> to { ErrorUIProxyImpl() }
            )
        )
    }
}
