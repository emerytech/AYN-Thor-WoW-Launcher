package app.gamenative.ui.screen.wow

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.File
import java.util.UUID
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Copy only an explicitly selected SAF tree. No raw shared-storage paths or broad permissions. */
internal object ScopedGameImport {
    internal fun safeChild(parent: File, name: String): File {
        require(name.isNotBlank() && name != "." && name != ".." && '/' !in name && '\\' !in name) { "Invalid file name" }
        return File(parent, name).also {
            require(it.canonicalFile.parentFile == parent.canonicalFile) { "File escapes import folder" }
        }
    }

    suspend fun copy(context: Context, tree: Uri): File {
        val base = File(context.getExternalFilesDir(null) ?: context.filesDir, "games").apply { check(isDirectory || mkdirs()) }
        val stage = File(base, ".import-${UUID.randomUUID()}").apply { check(mkdir()) }
        val destination = File(base, "game-${UUID.randomUUID()}")
        try {
            copyDirectory(context, tree, DocumentsContract.getTreeDocumentId(tree), stage)
            check(File(stage, ".build.info").isFile && File(stage, "Data").isDirectory) {
                "Select the installation folder containing .build.info and Data."
            }
            check(stage.renameTo(destination)) { "Could not finish import" }
            return destination
        } finally {
            // Only our disposable staging directory; never touch the source or an earlier import.
            stage.deleteRecursively()
        }
    }

    private suspend fun copyDirectory(context: Context, tree: Uri, id: String, destination: File) {
        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, id)
        val columns = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE)
        val cursor = checkNotNull(context.contentResolver.query(uri, columns, null, null, null)) { "Folder is unavailable" }
        cursor.use {
            while (it.moveToNext()) {
                currentCoroutineContext().ensureActive()
                val childId = it.getString(0)
                val name = it.getString(1)
                if (name.equals("login.txt", true)) continue
                val target = safeChild(destination, name)
                if (it.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR) {
                    check(target.mkdir()) { "Duplicate or unavailable folder" }
                    copyDirectory(context, tree, childId, target)
                } else {
                    check(!target.exists()) { "Duplicate file name" }
                    val document = DocumentsContract.buildDocumentUriUsingTree(tree, childId)
                    checkNotNull(context.contentResolver.openInputStream(document)) { "File is unavailable" }.use { input ->
                        target.outputStream().use { output ->
                            val buffer = ByteArray(1024 * 1024)
                            while (true) {
                                currentCoroutineContext().ensureActive()
                                val count = input.read(buffer)
                                if (count < 0) break
                                output.write(buffer, 0, count)
                            }
                        }
                    }
                }
            }
        }
    }
}
