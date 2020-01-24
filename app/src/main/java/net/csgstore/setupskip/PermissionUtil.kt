@file:Suppress("unused")

package net.csgstore.setupskip

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Service
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.provider.Settings
import android.util.Log


/**
 * @author Brian Shenk
 */
//@Suppress("unused")
//object PermissionUtil {

/*
public void setFirstTimeAskingPermission(Context context, String permission, boolean isFirstTime) {
    sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
//        SharedPreferences sharedPreference = activity.getSharedPreferences("License", MODE_PRIVATE);
    sharedPreferences.edit().putBoolean(permission, isFirstTime).apply();
}

public boolean isFirstTimeAskingPermission(Context context, String permission) {
    return getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(permission, true);
}*/

private fun Activity.initializeAlertBuilder() = AlertDialog.Builder(this).setCancelable(false)

/**
 * Returns true if the current app has Device Admin permissions active
 */
fun Context.isAdminActive(mDeviceAdmin: ComponentName): Boolean =
    checkIfAdminActive(devicePolicyManager, mDeviceAdmin)

fun Context.isAdminActive(adminReceiverName: String): Boolean =
    checkIfAdminActive(devicePolicyManager, ComponentName(this, adminReceiverName))

fun Context.isAdminActive(adminReceiver: Class<out DeviceAdminReceiver>): Boolean =
    checkIfAdminActive(devicePolicyManager, adminReceiver.componentName)

val Context.isThisDeviceOwner: Boolean get() = checkIfDeviceOwner(devicePolicyManager, packageName)

fun Activity.showActivateAdminPermissionRequest(adminReceiver: Class<out DeviceAdminReceiver>) {
    val mDeviceAdmin = ComponentName(packageName, adminReceiver.name)
    doActivateAdminPermissionRequest(mDeviceAdmin)
    isAdminActive(adminReceiver)
}

fun Activity.showActivateAdminPermissionRequest(adminReceiverNam: String) {
    val mDeviceAdmin = ComponentName(packageName, adminReceiverNam)
    doActivateAdminPermissionRequest(mDeviceAdmin)

}

fun Activity.showActivateAdminPermissionRequest(mDeviceAdmin: ComponentName) {
    doActivateAdminPermissionRequest(mDeviceAdmin)
}

private fun Activity.doActivateAdminPermissionRequest(mDeviceAdmin: ComponentName, reason: String? = null) {
    Log.d(TAG, "Sending user to device admin settings page")
    val adminActivateIntent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(
        DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin)
    val deviceAdminToast = "Please enable Device Admin.${if (reason != null) "\n$reason" else ""}"
    val alert = this.permissionDialog(adminActivateIntent, deviceAdminToast)
    alert.show()
}

/**
 * Ask for Write Settings permission
 */
fun Activity.showWriteSettingsPermissionRequest(reason: String? = null) {
    if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
        val writeSettingsIntent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:$packageName"))
        val writeSettingsToast =
            "Please enable Write Settings permissions.${if (reason != null) "\n$reason" else ""}"
        val alert = this.permissionDialog(writeSettingsIntent, writeSettingsToast)
        alert.show()
    }
}

@TargetApi(VERSION_CODES.LOLLIPOP)
fun Activity.showUsageStatsPermissionRequest(reason: String? = null) {
    /*        PackageManager packageManager = getPackageManager();
    try {
        final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
        Objects.requireNonNull(activity.appOpsManager).startWatchingMode("android:get_usage_stats",
                applicationInfo.packageName,
                (op, packageName) -> {
                    int mode = activity.appOpsManager.checkOpNoThrow(op, applicationInfo.uid, packageName);
                    mUsageStatsPermissionGranted = mode == 0;
                });
    } catch (PackageManager.NameNotFoundException | ActivityNotFoundException e) {
        e.printStackTrace();
    }
    return mUsageStatsPermissionGranted;*/
    /*        try {
        PackageManager packageManager = activity.getPackageManager();
        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(activity.getPackageName(), 0);
        int mode = Objects.requireNonNull(activity.appOpsManager).checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
        mUsageStatsPermissionGranted = (mode == AppOpsManager.MODE_ALLOWED);
        return mUsageStatsPermissionGranted;
    } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
        mUsageStatsPermissionGranted = true;
        return true;
    }*/
    val usageStatsIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    try {
        val usageStatsToast =
            "Please enable Usage Stats permissions.${if (reason != null) "\n$reason" else ""}"
        val alert = this.permissionDialog(usageStatsIntent, usageStatsToast)
        alert.show()
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }

}

fun Activity.showDrawOverlayPermissionRequest(reason: String? = null) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(
            this)) {
        return
    }
    val drawOverlayIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:" + packageName))
    val drawOverToast =
        "Please enable Draw Over Other Apps permissions.${if (reason != null) "\n$reason" else ""}"
    val alert = this.permissionDialog(drawOverlayIntent, drawOverToast)
    alert.show()
}

fun Activity.showLocationPermissionRequest(reason: String? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val locationToast =
                "Please enable Location permissions.${if (reason != null) "\n$reason" else ""}"
            val alertBuilder =
                AlertDialog.Builder(this).setCancelable(false).setMessage(locationToast)
                    .setPositiveButton("OK") { dialog, _ ->
                        if (!isLocationPermissionsGranted) {
                            requestPermissions(
                                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                LOCATION_PERMISSIONS_REQUEST_CODE)
                        } else {
                            startActivity(Intent().setComponent(
                                componentName))
                        }
                        dialog.dismiss()
                    }.create()
            alertBuilder.show()
        }
    }
}

val Context.isWriteSettingsPermissionGranted: Boolean get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(this)
fun DevicePolicyManager.isWriteSecureSettingsPermissionGranted(mDeviceAdmin: ComponentName): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.M || getPermissionGrantState(mDeviceAdmin, PACKAGE_NAME, Manifest.permission.WRITE_SECURE_SETTINGS) == 1

/**
 * @param accessibilityService service class
 * @return true if enabled, false if couldn't find or disabled
 */
fun Context.isAccessibilityServiceEnabled(accessibilityService: Class<out AccessibilityService>): Boolean {
    val enabledServicesSetting = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
    val flat = ComponentName(this,accessibilityService).flattenToString()
    val flatShort = ComponentName(this,accessibilityService).flattenToShortString()
    return enabledServicesSetting.split(":").any { s -> s == flat || s == flatShort }
}

fun Context.isMyServiceRunning(serviceClass: Class<out Service>): Boolean {
    for (service in (applicationContext.getSystemService(
        Context.ACTIVITY_SERVICE) as ActivityManager).getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) return true
    }
    return false
}

val Context.isDrawOverlayPermissionGranted: Boolean
    get() {
        return Build.VERSION.SDK_INT < VERSION_CODES.M || Settings.canDrawOverlays(this)
    }

val Context.isLocationPermissionsGranted: Boolean
    get() {
        return Build.VERSION.SDK_INT < VERSION_CODES.M || checkSelfPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

/*    public static void checkPermission(Context context, String permission, PermissionAskListener listener){
 *//*
     * If permission is not granted
     * *//*
        if (shouldAskPermission(context, permission)){
            *//*
     * If permission denied previously
     * *//*
            if (activity.shouldShowRequestPermissionRationale(permission)) {
                listener.onPermissionPreviouslyDenied();
            } else {
                *//*
     * Permission denied or first time requested
     * *//*
                if (PreferencesUtil.isFirstTimeAskingPermission(context, permission)) {
                    PreferencesUtil.setFirstTimeAskingPermission(context, permission, false);
                    listener.onNeedPermission();
                } else {
                    *//*
     * Handle the feature without permission or ask user to manually allow permission
     * *//*
                    listener.onPermissionDisabled();
                }
            }
        } else {
            listener.onPermissionGranted();
        }
    }*/
private const val LOCATION_PERMISSIONS_REQUEST_CODE = 87
//    private Context context;
private const val CODE_WRITE_SETTINGS_PERMISSION = 34
private const val USAGE_STATS_REQUEST_CODE = 2525 and -0x100
private const val ACCESSIBILITY_REQUEST_CODE = 1552 and -0x100
/**
 * code to post/handler request for permission
 */
private const val DRAW_OVERLAY_REQUEST_CODE = 5463 and -0x100

fun shouldAskPermission(context: Context, permission: String): Boolean {
    // Check if version is marshmallow and above.
    //Used in deciding to ask runtime permission
    if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
        val permissionResult = context.checkSelfPermission(permission)
        return permissionResult != PackageManager.PERMISSION_GRANTED
    }
    return false
}

private fun checkIfAdminActive(dpm: DevicePolicyManager, mDeviceAdmin: ComponentName): Boolean {
    return run {
        val state = dpm.isAdminActive(mDeviceAdmin)
        Log.d(TAG, "Administrator active?? $state")
        state
    }
}

private fun checkIfDeviceOwner(dpm: DevicePolicyManager, packageName: String): Boolean {
    return run {
        val state = dpm.isDeviceOwnerApp(packageName)
        Log.d(TAG, "Administrator active?? $state")
        state
    }
}

fun Activity.permissionDialog(intent: Intent, toast: String): AlertDialog.Builder {
    //        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    return AlertDialog.Builder(this).setTitle("Permission Required").setCancelable(false)
        .setMessage(toast).setPositiveButton("OK") { dialog, _ ->
            startActivity(intent)
            dialog.dismiss()
        }.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
}

/**
 * Callback on various cases on checking permission
 *
 * 1.  Below M, runtime permission not needed. In that case onPermissionGranted() would be called.
 *     If permission is already granted, onPermissionGranted() would be called.
 *
 * 2.  Above M, if the permission is being asked first time onNeedPermission() would be called.
 *
 * 3.  Above M, if the permission is previously asked but not granted, onPermissionPreviouslyDenied()
 *     would be called.
 *
 * 4.  Above M, if the permission is disabled by device policy or the user checked "Never ask again"
 *     check box on previous request permission, onPermissionDisabled() would be called.
 * */
interface PermissionAskListener {
    /*
     * Callback to ask permission
     * */
    fun onNeedPermission()

    /*
     * Callback on permission denied
     * */
    fun onPermissionPreviouslyDenied()

    /*
     * Callback on permission "Never show again" checked and denied
     * */
    fun onPermissionDisabled()

    /*
     * Callback on permission granted
     * */
    fun onPermissionGranted()
}
//}


//sealed class FlexibleDate<T> {
//    abstract val value: T
//
//    companion object {
//        @JvmStatic()
//        @JvmName("from")
//        operator fun invoke(value: String): FlexibleDate<String> = FlexibleDate - String(value)
//
//        @JvmStatic()
//        @JvmName("from")
//        operator fun invoke(value: Double): FlexibleDate<Double> = FlexibleDate - Double(value)
//
//        @JvmStatic()
//        @JvmName("from")
//        operator fun invoke(value: LocalDate): FlexibleDate<LocalDate> = FlexibleDate - LocalDate(value)
//    }
//
//    //Compiler generated classes, unusable directly from Kotlin or Java. Naming is arbitrary.
//    data class FlexibleDate-String(
//    override val value: String) : FlexibleDate<String>()
//
//    data class FlexibleDate-Double(
//    override val value: Double) : FlexibleDate<Double>()
//
//    data class FlexibleDate-LocalDate(
//    override val value: Double) : FlexibleDate<LocalDate>()
//}