package me.jameshunt.navfsm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

typealias UIRegistry = Map<KClass<UIProxy<*, *>>, () -> UIProxy<*, *>>
inline fun <reified Proxy : UIProxy<In, Out>, In, Out> UIRegistry.getProxyInstance(): Proxy = Proxy::class
    .let { it as? KClass<UIProxy<*, *>> ?: error("KClass is not a FlowUI: ${Proxy::class}") }
    .let { this[it] ?: error("missing ui registration for ${Proxy::class}")}
    .invoke() as Proxy

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

    fun init(
        scope: CoroutineScope,
        getInitialFlow: () -> FSM<Unit, Unit>,
        uiRegistry: UIRegistry,
        platformOperations: PlatformOperations,
        platformDependencies: PlatformDependencies
    ) {
        _platformDependencies = platformDependencies

        when (_root == null) {
            true -> scope.launch {
                _uiRegistry = uiRegistry
                _root = FSMTreeNode(
                    flow = getInitialFlow(),
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
            false -> root.platformOperations.resume()
        }
    }
}

