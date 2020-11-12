package me.jameshunt.navfsm

import kotlin.reflect.KClass

sealed class FSMResult<out T> {
    data class Complete<T>(val data: T) : FSMResult<T>()
    data class Error(val error: Throwable) : FSMResult<Nothing>()
    object Back : FSMResult<Nothing>()
}

interface FSM<Input, Output> {
    suspend fun run(input: Input): FSMResult<Output>
}

suspend fun <In, Out> FSM<*, *>.flow(flow: FSM<In, Out>, input: In): FSMResult<Out> {
    val node = FSMManager.root.getNodeFor(this) ?: throw IllegalStateException()

    val newFlowNode = FSMTreeNode(
        flow = flow,
        children = mutableListOf(),
        platformOperations = node.platformOperations // copy for now with no changes
    )
    node.children.add(newFlowNode)

    return flow.run(input)
}

suspend inline fun <reified In, reified Out> FSM<*, *>.flow(ui: KClass<*>, input: In): FSMResult<Out> {
    val uiInstance = FSMManager.uiRegistry.getUI<In, Out>(ui)
    val node = FSMManager.root.getNodeFor(this) ?: throw IllegalStateException()
    return node.platformOperations.showUI(uiInstance, input)
}

data class FSMTreeNode(
    val flow: FSM<*, *>,
    val children: MutableList<FSMTreeNode>,
    val platformOperations: PlatformOperations
) {
    fun getNodeFor(flow: FSM<*, *>): FSMTreeNode? {
        return when (this.flow == flow) {
            true -> this
            false -> children.asSequence().mapNotNull { it.getNodeFor(flow) }.first()
        }
    }
}

