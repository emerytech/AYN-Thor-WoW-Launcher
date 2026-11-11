package app.gamenative.ui.screen.wow

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Application-owned import: recreating the launcher UI must not cancel a large copy. */
internal object GameFolderImport {
    data class State(val running: Boolean = false, val bytes: Long = 0, val path: String? = null, val error: String? = null)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableState = MutableStateFlow(State())
    val state = mutableState.asStateFlow()

    @Synchronized
    fun start(context: Context, tree: Uri, flavor: WowFlavor) {
        if (mutableState.value.running) return
        val app = context.applicationContext
        mutableState.value = State(running = true)
        scope.launch {
            try {
                var lastReported = 0L
                val root = ScopedGameImport.copy(app, tree) { bytes ->
                    if (bytes - lastReported >= 16L * 1024 * 1024) {
                        lastReported = bytes
                        mutableState.value = State(running = true, bytes = bytes)
                    }
                }
                GamePath.save(app, root.absolutePath, flavor)
                mutableState.value = State(bytes = lastReported, path = root.absolutePath)
            } catch (e: CancellationException) {
                mutableState.value = State(error = "Import interrupted. Select the folder to try again.")
                throw e
            } catch (e: Exception) {
                mutableState.value = State(error = "Import failed: ${e.message}")
            }
        }
    }
}
