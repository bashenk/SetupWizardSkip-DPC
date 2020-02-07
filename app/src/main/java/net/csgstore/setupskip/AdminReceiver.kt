package net.csgstore.setupskip

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.lang.ref.WeakReference

fun Context.isAdminActive(): Boolean =
    checkIfAdminActive(devicePolicyManager, ComponentName(this, AdminReceiver::class.java))

fun Activity.showActivateAdminPermissionRequest(reason: String? = null) {
    val mDeviceAdmin = ComponentName(this, AdminReceiver::class.java)
    doActivateAdminPermissionRequest(mDeviceAdmin,  reason)
//    isAdminActive()
}

open class AdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.e(TAG, "Exception: AdminReceiver.onProvisioningComplete() invoked")
        val task = getPostProvisioningTask(context)
        if (!task.performPostProvisioningOperations(intent)) return
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        Log.e(TAG, "Exception: AdminReceiver.onProvisioningComplete() invoked")
        val task = getPostProvisioningTask(context)
        if (!task.performPostProvisioningOperations(intent)) return
        val launchIntent = task.getPostProvisioningLaunchIntent(intent)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            val msg = "AdminReceiver.onProvisioningComplete() invoked, but ownership not assigned"
            Log.e(TAG, msg)
            context.showToast(msg, Toast.LENGTH_LONG)
        }

        context.startActivity(Intent(context.applicationContext, ShortcutCreator::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        super.onProfileProvisioningComplete(context, intent)
    }

    companion object {
        const val INTENT_REMOVE_DEVICE_ADMIN = "net.csgstore.setupskip.REMOVE_DEVICE_ADMIN"
        const val INTENT_UNINSTALL = "$PACKAGE_NAME.UNINSTALL"
        const val INTENT_FACTORY_RESET = "$PACKAGE_NAME.FACTORY_RESET"
        private const val OBEY_DEPRECATION: Boolean = false
        val componentName: ComponentName by lazy {ComponentName(PACKAGE_NAME, AdminReceiver::class.java.name)}
        private lateinit var devicePolicyManager: DevicePolicyManager
        private lateinit var userManager: UserManager
        private lateinit var provisioningTask: PostProvisioningTask

        fun getPostProvisioningTask(context: Context): PostProvisioningTask {
//            if (!::provisioningTask.isInitialized)
                provisioningTask = PostProvisioningTask(WeakReference<Activity>(context as Activity))
            return provisioningTask
        }

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
                shortcutManager.requestPinShortcut(reset, resetSuccessCallback.intentSender)

                val removeAdminPinnedShortcutCallbackIntent =
                    shortcutManager.createShortcutResultIntent(removeAdmin)
                val removeAdminSuccessCallback =
                    PendingIntent.getBroadcast(context, /* request code */ 222,
                        removeAdminPinnedShortcutCallbackIntent, /* flags */ 0)
                shortcutManager.requestPinShortcut(removeAdmin, removeAdminSuccessCallback.intentSender)
            }
        }


        @RequiresApi(VERSION_CODES.M)
        fun configureAccessibility(context: Context) {
            initializeServices(context)
            devicePolicyManager.setPermissionGrantState(componentName, PACKAGE_NAME, Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
                DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)
//            context.dpm.setSecureSetting(componentName, Settings.Secure.ACCESSIBILITY_ENABLED, "1")
            val prevAccessibilityServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).split(";").filterNot { s -> s == componentName.flattenToString() }.joinToString { ";" }
            devicePolicyManager.setSecureSetting(componentName,
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
            initializeServices(context)
            if (!devicePolicyManager.setPermissionGrantState(componentName, PACKAGE_NAME,
                    Manifest.permission.WRITE_SETTINGS,
                    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED) || !context.isWriteSettingsPermissionGranted) throw SecurityException(
                "Permission ${Manifest.permission.WRITE_SETTINGS} was not granted")
            if (!devicePolicyManager.setPermissionGrantState(componentName, PACKAGE_NAME,
                    Manifest.permission.WRITE_SECURE_SETTINGS,
                    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED) || !devicePolicyManager.isWriteSecureSettingsPermissionGranted(
                    componentName)) throw SecurityException(
                "Permission ${Manifest.permission.WRITE_SECURE_SETTINGS} was not granted")
        }


        @RequiresApi(VERSION_CODES.M)
        private fun enablePermissions() {
//            if (!devicePolicyManager.setPermissionGrantState(AdminReceiver.componentName, PACKAGE_NAME, Manifest.permission.INSTALL_PACKAGES, DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)) throw SecurityException("Permission ${Manifest.permission.INSTALL_PACKAGES} was not granted")
            if (!devicePolicyManager.setPermissionGrantState(AdminReceiver.componentName, PACKAGE_NAME, Manifest.permission.DELETE_PACKAGES, DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)) throw SecurityException("Permission ${Manifest.permission.DELETE_PACKAGES} was not granted")
        }

        private fun initializeServices(context: Context) {
            if (!::devicePolicyManager.isInitialized)
                devicePolicyManager = context.devicePolicyManager
            if (!::userManager.isInitialized)
            userManager = context.userManager
        }


    }
}

class PublicReceiver : AdminReceiver() {}
