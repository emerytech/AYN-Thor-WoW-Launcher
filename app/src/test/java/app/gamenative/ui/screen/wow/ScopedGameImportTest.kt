package app.gamenative.ui.screen.wow

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class ScopedGameImportTest {
    @Test fun rejectsTraversalAndAbsoluteNamesFromProviders() {
        val base = File(System.getProperty("java.io.tmpdir"), "scoped-import-test")
        for (name in listOf("..", ".", "", "/etc/passwd", "../outside", "folder/file", "folder\\file")) {
            assertThrows(IllegalArgumentException::class.java) { ScopedGameImport.safeChild(base, name) }
        }
        assertEquals(File(base, ".build.info").canonicalFile, ScopedGameImport.safeChild(base, ".build.info").canonicalFile)
    }
}
