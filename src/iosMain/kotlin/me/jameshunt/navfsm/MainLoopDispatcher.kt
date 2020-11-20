package me.jameshunt.navfsm

import kotlinx.coroutines.*
import platform.darwin.*
import kotlin.coroutines.CoroutineContext

// https://github.com/Kotlin/kotlinx.coroutines/issues/470#issuecomment-440080970

@OptIn(InternalCoroutinesApi::class)
object MainLoopDispatcher : CoroutineDispatcher(), Delay {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val queue = dispatch_get_main_queue()
        dispatch_async(queue) {
            println("main Queue: $queue")
            block.run()
        }
    }


    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: CancellableContinuation<Unit>
    ) {
        dispatch_after(
            dispatch_time(DISPATCH_TIME_NOW, timeMillis * 1_000_000),
            dispatch_get_main_queue()
        ) {
            with(continuation) {
                resumeUndispatched(Unit)
            }
        }
    }

    @InternalCoroutinesApi
    override fun invokeOnTimeout(
        timeMillis: Long,
        block: Runnable,
        context: CoroutineContext
    ): DisposableHandle {
        val handle = object : DisposableHandle {
            var disposed = false
                private set

            override fun dispose() {
                disposed = true
            }
        }
        dispatch_after(
            dispatch_time(DISPATCH_TIME_NOW, timeMillis * 1_000_000),
            dispatch_get_main_queue()
        ) {
            if (!handle.disposed) {
                block.run()
            }
        }

        return handle
    }

}