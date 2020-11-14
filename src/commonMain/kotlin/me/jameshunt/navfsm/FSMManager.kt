package me.jameshunt.navfsm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

typealias UIRegistry = Map<KClass<UIProxy<*, *>>, () -> UIProxy<*, *>>
fun <In, Out> UIRegistry.getUI(ui: KClass<*>): UIProxy<In, Out> = ui
    .let { it as? KClass<UIProxy<*, *>> ?: error("KClass is not a FlowUI: $ui") }
    .let { this[it] ?: error("missing ui registration for $ui")}
    .invoke() as UIProxy<In, Out>

object FSMManager {
    private var _root: FSMTreeNode? = null
    val root: FSMTreeNode
        get() = _root ?: throw IllegalStateException()

    private var _uiRegistry: UIRegistry? = null
    val uiRegistry: UIRegistry
        get() = _uiRegistry ?: throw IllegalStateException()

    lateinit var platformDependencies: PlatformDependencies

    fun init(
        scope: CoroutineScope,
        initialFlow: FSM<Unit, Unit>,
        uiRegistry: UIRegistry,
        platformOperations: PlatformOperations
    ): Job {
        return scope.launch {
            _uiRegistry = uiRegistry
            _root = FSMTreeNode(
                flow = initialFlow,
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

