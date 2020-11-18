package me.jameshunt.navfsm

class IosFSMOperations(private val exposed: ExposedIosFSMOperations): PlatformFSMOperations {
    override suspend fun <In, Out> showUI(proxy: UIProxy<In, Out>, input: In): FSMResult<Out> {
        return exposed.showUI(proxy = proxy as UIProxy<Any?, Any?>, input = input).let {
            when (it) {
                is ExposedIosFSMOperations.Complete -> FSMResult.Complete(it.data as Out)
                is ExposedIosFSMOperations.Back -> FSMResult.Back
                is ExposedIosFSMOperations.Error -> FSMResult.Error(it.error)
                else -> TODO()
            }
        }
    }

    override fun duplicate(): PlatformFSMOperations {
        return this
    }
}

interface ExposedIosFSMOperations {
    interface ExposedResult
    data class Complete(val data: Any?): ExposedResult
    data class Error(val error: Throwable): ExposedResult
    class Back: ExposedResult

    suspend fun showUI(proxy: UIProxy<Any?, Any?>, input: Any?): ExposedResult
}
