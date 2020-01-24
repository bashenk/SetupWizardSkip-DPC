package net.csgstore.setupskip

import android.Manifest
import android.app.PendingIntent
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.wifi.WifiManager
import android.os.*
import android.os.Build.VERSION_CODES
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4ClassRunner::class)
open class AdminReceiver : DeviceAdminReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PROFILE_PROVISIONING_COMPLETE -> setDevicePolicySettings(context)
            //            Intent.ACTION_CREATE_SHORTCUT -> context.startActivity(Intent(context, MakeShortcuts::class.java))
            INTENT_FACTORY_RESET -> factoryReset(context)
            INTENT_REMOVE_DEVICE_ADMIN -> {
                removeDeviceAdminFromSelf(context)
                uninstallPackage(context)
            }
        }
        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context, intent: Intent) {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val adminName = getWho(context)
        val flags = when {
            Build.VERSION.SDK_INT >= VERSION_CODES.P -> DevicePolicyManager.SKIP_SETUP_WIZARD and DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED
            Build.VERSION.SDK_INT >= VERSION_CODES.N -> DevicePolicyManager.SKIP_SETUP_WIZARD
            else -> 0
        }
        val userHandle = if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
            context.devicePolicyManager.createAndManageUser(adminName, "Tester", adminName, null, flags)
        } else Binder.getCallingUserHandle()
        val serialNumber = userManager.getSerialNumberForUser(userHandle)
        setDevicePolicySettings(context)
        Log.i(TAG, "Device admin enabled for user with serial number: $serialNumber")
        super.onEnabled(context, intent)
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        setDevicePolicySettings(context)
        context.startActivity(Intent(context.applicationContext, Dummy::class.java))
        super.onProfileProvisioningComplete(context, intent)
    }

    private fun removeDeviceAdminFromSelf(context: Context) {
        val notOwnerOrAdministrator =
            "Setup Wizard Skip is already not a device owner/administrator"
        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < VERSION_CODES.O && context.isThisDeviceOwner) {
            try {
                @Suppress("DEPRECATION") context.devicePolicyManager.clearDeviceOwnerApp(PACKAGE_NAME)
                context.showToast(
                    "Successfully disabled device owner for package Setup Wizard Skip")
            } catch (e: Exception) {
                context.showToast("Setup Wizard Skip is already not a device owner")
            }
        } else if (!context.isThisDeviceOwner && context.isAdminActive(AdminReceiver::class.java)) {
            try {
                context.devicePolicyManager.removeActiveAdmin(getWho(context))
                context.showToast(
                    "Successfully disabled device administrator for package Setup Wizard Skip")
            } catch (e: Exception) {
                context.showToast(notOwnerOrAdministrator)
            }
        } else {
            context.showToast(notOwnerOrAdministrator)
        }
    }

    private fun Context.showToast(text: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
        }
    }

    private fun uninstallPackage(context: Context, packageName: String = PACKAGE_NAME): Boolean {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            val packageInstaller = context.packageManager.packageInstaller
            val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
            params.setAppPackageName(packageName)
            val sessionId = try {
                packageInstaller.createSession(params)
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            packageInstaller.uninstall(packageName,
                PendingIntent.getBroadcast(context, sessionId, Intent("android.intent.action.MAIN"),
                    0).intentSender)
            return true
        }
        System.err.println("old sdk")
        return false
    }

    private fun factoryReset(context: Context) {
        context.devicePolicyManager.wipeData(context.getResetFlags())
    }

    /**
     * Bit mask of additional options: currently supported flags are [DevicePolicyManager.WIPE_EXTERNAL_STORAGE] and
     * [DevicePolicyManager.WIPE_RESET_PROTECTION_DATA].
     *
     * Can be one or all of 0, [DevicePolicyManager.WIPE_EXTERNAL_STORAGE], and
     * [DevicePolicyManager.WIPE_RESET_PROTECTION_DATA].
     *
     * Apparently, [DevicePolicyManager.WIPE_EUICC] is as-yet unimplemented.
     *
     * @param  this@getFlags [Context] from which to retrieve the [DevicePolicyManager] and package name.
     * @return [Int]   The bitmask for the most destructive reset action possible with the given permissions.
     */
    //    @SuppressLint("ObsoleteSdkInt")
    private fun Context.getResetFlags(wipeResetProtection: Boolean = true, wipeExternalStorage: Boolean = false, wipeEUICC: Boolean = false): Int {
        var integer = 0
        if (wipeExternalStorage && Build.VERSION.SDK_INT > VERSION_CODES.JELLY_BEAN_MR2 && devicePolicyManager.isDeviceOwnerApp(
                packageName)) {
            integer = integer or DevicePolicyManager.WIPE_EXTERNAL_STORAGE
        }
        if (wipeResetProtection && Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1) {
            integer = integer or DevicePolicyManager.WIPE_RESET_PROTECTION_DATA
        }
        if (wipeEUICC && Build.VERSION.SDK_INT >= VERSION_CODES.P) {
            integer = integer or DevicePolicyManager.WIPE_EUICC
        }
        return integer
    }

    companion object {
        const val INTENT_REMOVE_DEVICE_ADMIN = "net.csgstore.setupskip.REMOVE_DEVICE_ADMIN"
        const val INTENT_FACTORY_RESET = "net.csgstore.setupskip.FACTORY_RESET"

        fun makeShortcuts(context: Context) {
            if (Build.VERSION.SDK_INT < VERSION_CODES.N_MR1) {
                Toast.makeText(context,
                    "Android version is too low to make shortcuts (requires 7.1 or higher)",
                    Toast.LENGTH_SHORT).show()
                return
            }
            val shortcutManager = context.applicationContext.getSystemService<ShortcutManager>(
                ShortcutManager::class.java) as ShortcutManager
            if (Build.VERSION.SDK_INT < VERSION_CODES.O || !shortcutManager.isRequestPinShortcutSupported) {
                Toast.makeText(context,
                    "PinShortcut is not supported (Home screen not supported, or Android version is below 8.0",
                    Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(Intent.ACTION_VIEW)
            intent.`package` = PACKAGE_NAME

            //            val reset = shortcutManager.manifestShortcuts.first { info -> info.id == "reset" }
            val reset = ShortcutInfo.Builder(context, "dynamic-reset")
                .setShortLabel(context.getString(R.string.reset_short_label))
                .setLongLabel(context.getString(R.string.reset_long_label))
                .setIcon(Icon.createWithResource(context, R.drawable.reset_foreground))
                .setActivity(ComponentName(context, Reset::class.java)).setIntent(intent).build()

            //            val removeAdmin = shortcutManager.manifestShortcuts.first { info -> info.id == "unlock" }
            val removeAdmin = ShortcutInfo.Builder(context, "dynamic-unlock")
                .setShortLabel(context.getString(R.string.unlock_short_label))
                .setLongLabel(context.getString(R.string.unlock_long_label))
                .setIcon(Icon.createWithResource(context, R.drawable.unlock_foreground))
                .setActivity(ComponentName(context, Unlock::class.java)).setIntent(intent).build()

            //            shortcutManager.dynamicShortcuts = listOf(reset, removeAdmin)
            if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
                val resetPinnedShortcutCallbackIntent =
                    shortcutManager.createShortcutResultIntent(reset)
                val resetSuccessCallback =
                    PendingIntent.getBroadcast(context, /* request code */ 111,
                        resetPinnedShortcutCallbackIntent, /* flags */ 0)
                shortcutManager.requestPinShortcut(reset, null)

                val removeAdminPinnedShortcutCallbackIntent =
                    shortcutManager.createShortcutResultIntent(removeAdmin)
                val removeAdminSuccessCallback =
                    PendingIntent.getBroadcast(context, /* request code */ 222,
                        removeAdminPinnedShortcutCallbackIntent, /* flags */ 0)
                shortcutManager.requestPinShortcut(removeAdmin, null)
            }
        }


        @RequiresApi(VERSION_CODES.M)
        fun configureAccessibility(context: Context) {
            context.devicePolicyManager.setPermissionGrantState(componentName, PACKAGE_NAME,
                Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
                DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)
//            context.dpm.setSecureSetting(componentName, Settings.Secure.ACCESSIBILITY_ENABLED, "1")
            val prevAccessibilityServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).split(";").filterNot { s -> s == componentName.flattenToString() }.joinToString { ";" }
            context.devicePolicyManager.setSecureSetting(componentName,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                "$prevAccessibilityServices;${componentName.flattenToString()}")
        }

//        @RequiresApi(Build.VERSION_CODES.N)
//        fun enableAccessibilityService(context: Context) {
//            if (context.isAccessibilityServiceEnabled(MyAccessibilityService::class.java)) return
//            val prevAccessibilityServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).split(":").filterNot { s -> s.isEmpty() }
//            val flatComponentName = MyAccessibilityService.componentName.flattenToString()
//            val newAccessibilityServiceList = arrayOf(flatComponentName, *prevAccessibilityServices.toTypedArray());
//            if (newAccessibilityServiceList.isEmpty()) return
//            val newString = newAccessibilityServiceList.joinToString(separator = ":")
//            val cmd = "settings put secure enabled_accessibility_services $newString"
//            InstrumentationRegistry.getInstrumentation().getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
//                .executeShellCommand(cmd).close()
//            TimeUnit.SECONDS.sleep(3)
//        }

        @RequiresApi(VERSION_CODES.M)
        fun enableSettingsPermissions(context: Context) {
            if (!context.devicePolicyManager.setPermissionGrantState(componentName, PACKAGE_NAME,
                    Manifest.permission.WRITE_SETTINGS,
                    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED) || !context.isWriteSettingsPermissionGranted) throw SecurityException(
                "Permission ${Manifest.permission.WRITE_SETTINGS} was not granted")
            if (!context.devicePolicyManager.setPermissionGrantState(componentName, PACKAGE_NAME,
                    Manifest.permission.WRITE_SECURE_SETTINGS,
                    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED) || !context.devicePolicyManager.isWriteSecureSettingsPermissionGranted(
                    componentName)) throw SecurityException(
                "Permission ${Manifest.permission.WRITE_SECURE_SETTINGS} was not granted")
        }

        fun setDevicePolicySettings(context: Context, shouldSetOn: Boolean = true) {
            val setting = if (shouldSetOn) "1" else "0"
            context.devicePolicyManager.setGlobalSetting(componentName, Settings.Global.ADB_ENABLED, setting)
            context.devicePolicyManager.setGlobalSetting(componentName,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, setting)
            if (Build.VERSION.SDK_INT < VERSION_CODES.M) {
                // Changing the following settings has no effect as of {@link android.os.Build.VERSION_CODES#M}
                context.devicePolicyManager.setGlobalSetting(componentName,
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, setting)
                context.devicePolicyManager.setGlobalSetting(componentName, Settings.Global.WIFI_ON, setting)
            } else {
                context.devicePolicyManager.setKeyguardDisabled(componentName, shouldSetOn)
                if (shouldSetOn) {
                    (context.applicationContext.getSystemService(
                        Context.WIFI_SERVICE) as WifiManager).isWifiEnabled = true
                }
                val plugInState =
                    if (shouldSetOn) BatteryManager.BATTERY_PLUGGED_AC or BatteryManager.BATTERY_PLUGGED_USB else 0
                // This setting is only available from {@link android.os.Build.VERSION_CODES#M} onwards and can only be set if {@link #setMaximumTimeToLock} is not used to set a timeout.
                context.devicePolicyManager.setGlobalSetting(componentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN, plugInState.toString())
            }
        }
    }
}

class PublicReceiver : AdminReceiver() {}
