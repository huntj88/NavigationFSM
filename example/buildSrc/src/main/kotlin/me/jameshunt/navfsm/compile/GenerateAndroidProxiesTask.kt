package me.jameshunt.navfsm.compile

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.intellij.lang.annotations.Language
import java.io.File
import java.util.concurrent.TimeUnit

open class GenerateAndroidProxiesTask : DefaultTask() {

    @TaskAction
    fun run() {
        println("Running GenerateAndroidProxiesTask")

        val lines = listOf(
            "grep", "-r", "-E",
            "class[ ]+[a-zA-Z0-9]+[ ]*:[ ]*NavFSM(Dialog|)Fragment[ ]*<[a-zA-Z, .0-9]+>[ ]*()",
            "../example/app"
        ).runCommand().lines().filter { it.isNotBlank() }

        val usageInfo = lines.map { getUsageInfo(it) }

        usageInfo.forEach { generateProxy(it) }
    }

    private fun getUsageInfo(classSignature: String): InfoForUIProxy {
        println(classSignature)

        val uiName = "class[ ]+([a-zA-Z0-9]+)[ ]*:".toRegex().find(classSignature)!!.groupValues[1]

        val packageName = "[./\\w]+(java|kotlin)/([/\\w]+)/[\\w]+\\.kt"
            .toRegex()
            .find(classSignature)!!.groupValues[2]
            .replace("/", ".")


        val isFragment = ":[ ]*NavFSMFragment[ ]*<".toRegex().containsMatchIn(classSignature)
        if (!isFragment) {
            val isDialog = ":[ ]*NavFSMDialogFragment[ ]*<".toRegex().containsMatchIn(classSignature)
            check(isDialog)
        }

        return "Fragment[ ]*<[ ]*([\\w.]+)[ ]*,[ ]*([\\w.]+)[ ]*,[ ]*([\\w.]+)[ ]*>[ ]*\\(\\)"
            .toRegex()
            .find(classSignature)!!
            .groupValues
            .let { groups ->
                InfoForUIProxy(
                    importUI = "$packageName.$uiName",
                    uiClassName = uiName,
                    uiType = when (isFragment) {
                        true -> InfoForUIProxy.UIType.Fragment
                        false -> InfoForUIProxy.UIType.Dialog
                    },
                    chosenProxy = groups[1],
                    input = groups[2],
                    output = groups[3]
                )
            }

    }

    private fun generateProxy(info: InfoForUIProxy) {
        
        val fragmentOrDialogCode = when (info.uiType) {
            InfoForUIProxy.UIType.Fragment -> """
                override var fragment: WeakReference<NavFSMFragment<*, *, *>>? = null
                override fun newFragmentInstance(): NavFSMFragment<*, *, *> = ${info.uiClassName}()
                override fun bind(fragment: NavFSMFragment<*, *, *>) {
                    fragment as ${info.uiClassName}
                    this.restoreState(fragment)
                    this.fragment = WeakReference(fragment)
                    fragment.proxy = this
                }
                
                internal fun saveState() {
                    this.fragment?.get()?.let {
                        // We can't save the state of a Fragment that isn't added to a FragmentManager.
                        if (it.isAdded) {
                            this.state = it.fragmentManager?.saveFragmentInstanceState(it)
                        }
                    }
                }
                
                private fun restoreState(fragment: NavFSMFragment<*, *, *>) {
                    this.state?.let {
                        // Can't set initial state if already added
                        if (!fragment.isAdded) {
                            fragment.setInitialSavedState(this.state)
                        }
                    }
                }
            """
            InfoForUIProxy.UIType.Dialog -> """
                override var dialog: WeakReference<NavFSMDialogFragment<*, *, *>>? = null
                override fun newDialogInstance(): NavFSMDialogFragment<*, *, *> = ${info.uiClassName}()
                override fun bind(dialog: NavFSMDialogFragment<*, *, *>) {
                    dialog as ${info.uiClassName}
                    this.restoreState(dialog)
                    this.dialog = WeakReference(dialog)
                    dialog.proxy = this
                }
                
                internal fun saveState() {
                    this.dialog?.get()?.let {
                        // We can't save the state of a Fragment that isn't added to a FragmentManager.
                        if (it.isAdded) {
                            this.state = it.fragmentManager?.saveFragmentInstanceState(it)
                        }
                    }
                }
            
                private fun restoreState(dialog: NavFSMDialogFragment<*, *, *>) {
                    this.state?.let {
                        // Can't set initial state if already added
                        if (!dialog.isAdded) {
                            dialog.setInitialSavedState(this.state)
                        }
                    }
                }
            """
        }
        
        
        @Language("kotlin")
        val blah = """
            import ${info.importUI}
            import androidx.fragment.app.Fragment
            import kotlinx.coroutines.CompletableDeferred
            import me.jameshunt.navfsm.DialogProxy
            import me.jameshunt.navfsm.FSMResult
            import me.jameshunt.navfsm.NavFSMDialogFragment
            import me.jameshunt.navfsm.NavFSMFragment
            import me.jameshunt.navfsm.test.ErrorUIProxy
            import java.lang.ref.WeakReference
            import java.util.UUID
            
            class ${info.chosenProxy}Generated : ${info.chosenProxy}, ${info.uiType.name}Proxy {
                override val tag: String = UUID.randomUUID().toString()

                override var completableDeferred: CompletableDeferred<FSMResult<${info.output}>> =
                    CompletableDeferred(FSMResult.Back)
                    get() {
                        field = if (field.isActive) field else CompletableDeferred()
                        return field
                    }

                private var state: Fragment.SavedState? = null
                override var input: ${info.input}? = null

                override fun complete(data: ${info.output}) {
                    saveState()
                    completableDeferred.complete(FSMResult.Complete(data))
                }

                override fun back() {
                    saveState()
                    completableDeferred.complete(FSMResult.Back)
                }

                override fun error(error: Throwable) {
                    state = null
                    completableDeferred.complete(FSMResult.Error(error))
                }

                $fragmentOrDialogCode
            }
        """.trimIndent()

        println(blah)
    }
}

data class InfoForUIProxy(
    val importUI: String,
    val uiClassName: String,
    val uiType: UIType,
    val chosenProxy: String,
    val input: String,
    val output: String
) {
    enum class UIType {
        Fragment,
        Dialog
    }
}

fun List<String>.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String = ProcessBuilder(this)
    .directory(workingDir)
    .redirectOutput(ProcessBuilder.Redirect.PIPE)
    .redirectError(ProcessBuilder.Redirect.PIPE)
    .start().apply { waitFor(timeoutAmount, timeoutUnit) }
    .inputStream.bufferedReader().readText()
