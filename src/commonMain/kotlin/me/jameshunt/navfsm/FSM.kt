package me.jameshunt.navfsm

interface State

sealed class FSMResult<out T> {
    data class Complete<T>(val data: T) : FSMResult<T>()
    data class Error(val error: Throwable) : FSMResult<Nothing>()
    object Back : FSMResult<Nothing>()
}

suspend fun <Result, NextState : State> FSMResult<Result>.onResult(
    onComplete: suspend (Result) -> NextState = { TODO() },
    onBack: suspend () -> NextState = { TODO() },
    onError: suspend (Throwable) -> NextState = { TODO() }
): NextState = when (this) {
    is FSMResult.Complete -> onComplete(this.data)
    is FSMResult.Back -> onBack()
    is FSMResult.Error -> onError(this.error)
}

interface FSM<Input, Output> {
    suspend fun run(input: Input): FSMResult<Output>
}

suspend fun <In, Out> FSM<*, *>.flow(flow: FSM<In, Out>, input: In): FSMResult<Out> {
    val node = FSMManager.root.getNodeFor(this) ?: throw IllegalStateException()

    val newFlowNode = FSMTreeNode(
        flow = flow,
        children = mutableListOf(),
        platformFSMOperations = node.platformFSMOperations.duplicate() // copy for now with no changes
    )
    node.children.add(newFlowNode)

    return flow.run(input).also { node.children.remove(newFlowNode) }
}

suspend inline fun <reified Proxy : UIProxy<In, Out>, In, Out> FSM<*, *>.flow(
    proxy: Proxy,
    input: In
): FSMResult<Out> {
    proxy.input = input
    println("kotlin input is: $input, for proxy: $proxy")
    val node = FSMManager.root.getNodeFor(this) ?: throw IllegalStateException()
    return node.platformFSMOperations.showUI(proxy).also { println("Also, output: $it") }
}

data class FSMTreeNode(
    val flow: FSM<*, *>,
    val children: MutableList<FSMTreeNode>,
    val platformFSMOperations: PlatformFSMOperations
) {
    fun getNodeFor(flow: FSM<*, *>): FSMTreeNode? {
        return when (this.flow == flow) {
            true -> this
            false -> children.asSequence().mapNotNull { it.getNodeFor(flow) }.first()
        }
    }

    fun walkTreeForOperation(operation: (FSMTreeNode) -> Unit): Boolean {
        this.children.firstOrNull()?.let {
            if (!it.walkTreeForOperation(operation)) {
                operation(it)
            }
            return true
        }

        return when (this == FSMManager.root) {
            true -> {
                operation(this)
                true
            }
            false -> false
        }
    }
}

