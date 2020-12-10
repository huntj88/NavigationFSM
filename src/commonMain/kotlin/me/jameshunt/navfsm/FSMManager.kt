package me.jameshunt.navfsm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.native.concurrent.ThreadLocal
import kotlin.reflect.KClass

inline fun <reified Proxy : UIProxy<*, *>> FSMManager.proxy(): Proxy {
    return Proxy::class
        .let { it as KClass<UIProxy<*, *>> }
        .let { uiRegistry[it] ?: error("missing ui registration for ${Proxy::class}") }
        .invoke() as Proxy
}

typealias UIRegistry = Map<KClass<UIProxy<*, *>>, () -> UIProxy<*, *>>

@ThreadLocal
object FSMManager {
    private var _root: FSMTreeNode? = null
    val root: FSMTreeNode
        get() = _root ?: throw IllegalStateException()

    private var _uiRegistry: UIRegistry? = null
    val uiRegistry: UIRegistry
        get() = _uiRegistry ?: throw IllegalStateException()

    var platformDependencies: PlatformDependencies? = null

    private var _initialFlow: (() -> FSM<Unit, Unit>)? = null
    val initialFlow: () -> FSM<Unit, Unit>
        get() = _initialFlow ?: throw IllegalStateException()

    private var _scope: CoroutineScope? = null
    val scope: CoroutineScope
        get() = _scope ?: throw IllegalStateException()

    fun config(
        scope: CoroutineScope,
        uiRegistry: UIRegistry, // generate UI registry bindings
        getInitialFlow: () -> FSM<Unit, Unit>,
    ) {
        _scope = scope
        _uiRegistry = uiRegistry
        _initialFlow = getInitialFlow
    }

    fun isInitialized(): Boolean = _root != null

    fun init(fsmOperations: PlatformFSMOperations, platformDependencies: PlatformDependencies) {
        check(!isInitialized()) { "Already initialized" }
        this.platformDependencies = platformDependencies
        scope.launch {
            _uiRegistry = uiRegistry
            _root = FSMTreeNode(
                flow = SimpleGroupFSM(initialFlow()),
                children = mutableListOf(),
                platformFSMOperations = fsmOperations
            )

            (root.flow as FSM<Unit, Unit>)
                .run(Unit)
                .let {
                    when (it) {
                        is FSMResult.Complete, is FSMResult.Back -> platformDependencies.flowEnd()
                        is FSMResult.Error -> throw it.error
                    }
                }
                .also { _root = null }
        }
    }
}
