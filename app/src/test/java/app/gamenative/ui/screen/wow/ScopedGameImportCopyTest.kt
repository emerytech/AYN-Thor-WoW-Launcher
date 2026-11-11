package app.gamenative.ui.screen.wow

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.MatrixCursor
import android.net.Uri
import android.provider.DocumentsContract
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ScopedGameImportCopyTest {
    private fun context(root: File, fail: Boolean): Context {
        val context = mockk<Context>()
        val resolver = mockk<ContentResolver>()
        every { context.getExternalFilesDir(null) } returns root
        every { context.contentResolver } returns resolver
        every { resolver.query(any(), any(), any(), any(), any()) } answers {
            val uri = firstArg<Uri>()
            val columns = arrayOf("document_id", "_display_name", "mime_type")
            MatrixCursor(columns).apply {
                if (DocumentsContract.getDocumentId(uri) == "root") {
                    addRow(arrayOf("build", ".build.info", "text/plain"))
                    addRow(arrayOf("data", "Data", DocumentsContract.Document.MIME_TYPE_DIR))
                    addRow(arrayOf("secret", "login.txt", "text/plain"))
                } else addRow(arrayOf("asset", "data.bin", "application/octet-stream"))
            }
        }
        every { resolver.openInputStream(any()) } answers {
            if (fail) throw IOException("Simulated provider failure")
            ByteArrayInputStream(DocumentsContract.getDocumentId(firstArg()).toByteArray())
        }
        return context
    }

    @Test fun importsSelectedTreeWithoutCopyingTransientCredentials() = runBlocking {
        val root = Files.createTempDirectory("scoped-copy").toFile()
        try {
            val imported = ScopedGameImport.copy(context(root, false), Uri.parse("content://fixture/tree/root"))
            assertEquals("build", File(imported, ".build.info").readText())
            assertEquals("asset", File(imported, "Data/data.bin").readText())
            assertFalse(File(imported, "login.txt").exists())
        } finally { root.deleteRecursively() }
    }

    @Test fun failedImportRemovesOnlyItsStagingFiles() = runBlocking {
        val root = Files.createTempDirectory("scoped-copy-failure").toFile()
        try {
            val existing = File(root, "games/previous").apply { mkdirs() }
            File(existing, "keep").writeText("prior import")
            try {
                ScopedGameImport.copy(context(root, true), Uri.parse("content://fixture/tree/root"))
                fail("Expected provider failure")
            } catch (_: IOException) { }
            assertEquals("prior import", File(existing, "keep").readText())
            assertEquals(listOf("previous"), File(root, "games").listFiles()!!.map { it.name })
        } finally { root.deleteRecursively() }
    }
}
