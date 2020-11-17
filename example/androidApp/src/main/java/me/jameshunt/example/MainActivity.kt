package me.jameshunt.example

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import me.jameshunt.navfsm.*
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        layoutInflater.inflate(R.layout.fullscreen_fragment_container, navFSMContainer, true)

        val platformDependencies = AndroidDependencies(
            fragmentManager = supportFragmentManager,
            flowFinished = { super.onBackPressed() }
        )

        when (FSMManager.isInitialized()) {
            true -> {
                FSMManager.platformDependencies = platformDependencies
                platformDependencies.resume()
            }
            false -> FSMManager.init(
                platformDependencies = platformDependencies,
                fsmOperations = AndroidFSMOperations(R.id.fragment_container)
            )
        }
    }

    override fun onBackPressed() {
        (FSMManager.platformDependencies as AndroidDependencies).back()
    }
}

class App : Application(), NavFSMConfig by NavFSMConfigImpl()

interface NavFSMConfig {
    val fsmNavScope: CoroutineScope
}

class NavFSMConfigImpl: NavFSMConfig {
    private  val job = Job()
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
