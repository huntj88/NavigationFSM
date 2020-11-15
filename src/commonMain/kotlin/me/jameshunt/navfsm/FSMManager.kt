package me.jameshunt.navfsm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

inline fun <reified Proxy : UIProxy<In, Out>, In, Out> FSMManager.proxy(): Proxy {
    return Proxy::class
        .let { it as KClass<UIProxy<*, *>> }
        .let { uiRegistry[it] ?: error("missing ui registration for ${Proxy::class}") }
        .invoke() as Proxy
}

typealias UIRegistry = Map<KClass<UIProxy<*, *>>, () -> UIProxy<*, *>>

object FSMManager {
    private var _root: FSMTreeNode? = null
    val root: FSMTreeNode
        get() = _root ?: throw IllegalStateException()

    private var _uiRegistry: UIRegistry? = null
    val uiRegistry: UIRegistry
        get() = _uiRegistry ?: throw IllegalStateException()

    private var _platformDependencies: PlatformDependencies? = null
    val platformDependencies: PlatformDependencies
        get() = _platformDependencies ?: throw IllegalStateException()

    private var _initialFlow: (() -> FSM<Unit, Unit>)? = null
    val initialFlow: () -> FSM<Unit, Unit>
        get() = _initialFlow ?: throw IllegalStateException()

    private var _scope: CoroutineScope? = null
    val scope: CoroutineScope
        get() = _scope ?: throw IllegalStateException()

    fun resume(platformDependencies: PlatformDependencies) {
        _platformDependencies = platformDependencies
        platformDependencies.resume()
    }

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

    fun init(platformOperations: PlatformOperations, platformDependencies: PlatformDependencies) {
        _platformDependencies = platformDependencies
        scope.launch {
            _uiRegistry = uiRegistry
            _root = FSMTreeNode(
                flow = initialFlow(),
                children = mutableListOf(),
                platformOperations = platformOperations
            )

            (root.flow as FSM<Unit, Unit>).run(Unit).let {
                when (it) {
                    is FSMResult.Complete -> TODO()
                    is FSMResult.Error -> throw it.error
                    is FSMResult.Back -> TODO()
                }
            }
        }
    }
}

