package app.gamenative

import android.app.Application
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import app.gamenative.ui.screen.wow.WowClientDownloader
import app.gamenative.utils.AppUpdater
import app.gamenative.utils.StorageUtils
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PlayDistributionTest {
    @Test fun manifestDoesNotRequestBroadStorageOrApkInstall() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val permissions = app.packageManager.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions.orEmpty().toSet()
        for (permission in listOf("MANAGE_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE", "READ_EXTERNAL_STORAGE", "REQUEST_INSTALL_PACKAGES", "READ_LOGS")) {
            assertFalse(permission, "android.permission.$permission" in permissions)
        }
        assertTrue(BuildConfig.PLAY_STORE)
        assertEquals("com.emerytech.handheldruntime", BuildConfig.APPLICATION_ID)
    }

    @Test fun appAndGameUpdateChecksDoNotContactRemoteServers() = runBlocking {
        assertNull(AppUpdater.check())
        assertNull(WowClientDownloader.checkVersion(File("/missing")))
        assertThrows(IllegalStateException::class.java) { WowClientDownloader.download(File("/missing")) {} }
        Unit
    }

    @Test fun storagePermissionDoesNotAcceptPathsThatMerelyContainSandboxName() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        assertTrue(StorageUtils.hasStoragePermission(app, app.filesDir.path))
        assertFalse(StorageUtils.hasStoragePermission(app, app.filesDir.path + "-outside"))
        assertFalse(StorageUtils.hasStoragePermission(app, "/sdcard/Download"))
    }
}
