package net.csgstore.setupskip

import android.Manifest
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.IPackageDeleteObserver
import android.content.pm.IPackageInstallObserver
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


/**
 * Utility class for various operations necessary to package installation.
 */
class PackageInstallationUtils(private val context: Context) {
    private val packageManager: PackageManager = context.packageManager
    private val packageInstaller: PackageInstaller = packageManager.packageInstaller
    private val installObserver: PackageInstallObserver = PackageInstallObserver()
    private val deleteObserver: PackageDeleteObserver = PackageDeleteObserver()
    private val installTypes = arrayOf<Class<*>?>(Uri::class.java, IPackageInstallObserver::class.java, Integer.TYPE, String::class.java)
//    private val installTypes = arrayOf<Class<out Any>>(Uri::class.java, IPackageInstallObserver::class.java, Integer.TYPE, String::class.java)
    private val uninstallTypes = arrayOf<Class<*>?>(String::class.java, IPackageDeleteObserver::class.java, Integer.TYPE)
//    private val uninstallTypes = arrayOf<Class<*>>(String::class.java, IPackageDeleteObserver::class.java, Integer.TYPE)

    @get:Throws(SecurityException::class, NoSuchMethodException::class)
    private val installMethod: Method
        get() = packageManager.javaClass.getMethod("installPackage", *installTypes)

    @get:Throws(SecurityException::class, NoSuchMethodException::class)
    private val uninstallMethod: Method
        get() = packageManager.javaClass.getMethod("deletePackage", *uninstallTypes)

    @Throws(IOException::class)
    fun installPackage(inputStream: InputStream, packageName: String?): Boolean {
        val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(packageName)
        // set params
        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)
        val out: OutputStream = session.openWrite("SetupSkipUninstall", 0, -1)
        val buffer = ByteArray(65536)
        var c: Int
        while (inputStream.read(buffer).also { c = it } != -1) {
            out.write(buffer, 0, c)
        }
        session.fsync(out)
        inputStream.close()
        out.close()
        session.commit(createInstallIntentSender(context, sessionId))
        return true
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.N)
    @RequiresPermission(anyOf = [Manifest.permission.DELETE_PACKAGES, Manifest.permission.REQUEST_DELETE_PACKAGES])
    fun uninstallPackageOld(packageName: String) {
        packageInstaller.uninstall(packageName, createUninstallIntentSender(context, packageName))
    }

    @Throws(java.lang.IllegalArgumentException::class, IllegalAccessException::class, InvocationTargetException::class)
    fun installPackage(apkFile: String, @InstallFlags flags: Int = PackageManagerFlags.Installer.INSTALL_REPLACE_EXISTING) {
        installPackage(File(apkFile), flags)
    }

    @Throws(java.lang.IllegalArgumentException::class, IllegalAccessException::class, InvocationTargetException::class)
    fun installPackage(apkFile: File, @InstallFlags flags: Int = PackageManagerFlags.Installer.INSTALL_REPLACE_EXISTING) {
        require(apkFile.exists())
        val packageURI = Uri.fromFile(apkFile)
        installPackage(packageURI, flags)
    }

    @Throws(java.lang.IllegalArgumentException::class, IllegalAccessException::class, InvocationTargetException::class)
    fun installPackage(apkFile: Uri?, @InstallFlags flags: Int = PackageManagerFlags.Installer.INSTALL_REPLACE_EXISTING) {
        installMethod.invoke(packageManager, *arrayOf<Any?>(apkFile, installObserver, flags, null))
    }

    @RequiresPermission("android.permission.INTERACT_ACROSS_USERS_FULL")
    fun uninstallViaRuntime(packageName: String) {
        Runtime.getRuntime().exec("pm uninstall $packageName")
        Log.e(TAG, "running \"pm uninstall $packageName\"")
    }

    @RequiresPermission(Manifest.permission.DELETE_PACKAGES)
    @Throws(IllegalArgumentException::class, IllegalAccessException::class, InvocationTargetException::class)
    fun uninstallPackage(packageName: String) {
        uninstallMethod.invoke(packageManager, *arrayOf<Any?>(packageName, deleteObserver, 0))
        Log.e(TAG, "Running \"uninstallPackage($packageName\")")
    }

    @RequiresPermission(Manifest.permission.DELETE_PACKAGES)
    fun deleteTest(packageName: String) {
        // how I use the installPackage method
//        installPackage.invoke(context.packageManager, apkUri, installObserver, INSTALL_REPLACE_EXISTING, INSTALLER_NAME)
    }

    private fun createInstallIntentSender(context: Context, sessionId: Int): IntentSender =
        PendingIntent.getBroadcast(context, sessionId, Intent(Companion.ACTION_INSTALL_COMPLETE), 0).intentSender

    @RequiresApi(Build.VERSION_CODES.N)
    private fun createUninstallIntentSender(context: Context, packageName: String): IntentSender =
        PendingIntent.getBroadcast(context, 0,
            Intent(Companion.ACTION_UNINSTALL_COMPLETE).putExtra(Intent.EXTRA_PACKAGE_NAME, packageName), 0).intentSender

    companion object {
        const val ACTION_INSTALL_COMPLETE = "$PACKAGE_NAME.INSTALL_COMPLETE"
        const val ACTION_UNINSTALL_COMPLETE = "$PACKAGE_NAME.UNINSTALL_COMPLETE"
    }
}

class InstallationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.action) {
            PackageInstallationUtils.ACTION_UNINSTALL_COMPLETE -> {
                context?.showToast("Uninstallation complete!", Toast.LENGTH_LONG)
            }
            PackageInstallationUtils.ACTION_INSTALL_COMPLETE -> {
                context?.showToast("Installation complete!", Toast.LENGTH_LONG)
            }
        }
    }
}

private val onInstalledPackaged: OnInstalledPackaged? = null

interface OnInstalledPackaged {
    fun packageInstalled(packageName: String?, returnCode: Int)
}

internal class PackageInstallObserver : IPackageInstallObserver.Stub() {
    @Throws(RemoteException::class)
    override fun packageInstalled(packageName: String, returnCode: Int) {
        onInstalledPackaged?.packageInstalled(packageName, returnCode)
    }
}

internal class PackageDeleteObserver : IPackageDeleteObserver.Stub() {
    @Throws(RemoteException::class)
    override fun packageDeleted(packageName: String, returnCode: Int) {
        onInstalledPackaged?.packageInstalled(packageName, returnCode)
    }
}



/*
@RequiresPermission(Manifest.permission.DELETE_PACKAGES)
fun deletePackage(context: Context, packageName: String, @DeleteFlags flags: Int) {

    // get the classes and method via reflection
    val cPackageInstallObserver = Class.forName("android.content.pm.IPackageInstallObserver")
    val packageManagerClass = PackageManager::class.java
    val installPackage = packageManagerClass.getMethod("installPackage", Uri::class.java, cPackageInstallObserver, Integer.TYPE, String::class.java)
    val deletePackage = packageManagerClass.getMethod("deletePackage", String::class.java, cPackageInstallObserver, Integer.TYPE)
    // create the observer
    val installObserver = Proxy.newProxyInstance(
        PackageInstallationUtils::class.java.classLoader,
        arrayOf<Class<*>>(IPackageDeleteObserver::class.java),  // local definition
        InstallObserverInvocationHandler(listener)
    ) as IPackageInstallObserver // local definition

    // how I use the installPackage method
    deletePackage.invoke(context.packageManager, packageName, installObserver, flags)

    //        val f = PackageManager::class..find { it.name == "world" }
    //        f?.let {
    //            it.isAccessible = true
    //            val w = it.get(hello) as World
    //            println(w.foo())
    //        }
}

// the invocation handler
class InstallObserverInvocationHandler(private val listener: PackageListener): InvocationHandler {

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        if (method?.name == "packageInstalled" && method.parameterTypes.size == 2 &&
            method.parameterTypes[0] == String::class.java &&
            method.parameterTypes[1] == Integer.TYPE) {

            */
/* custom implementation *//*

        }
        return null
    }
}*/
