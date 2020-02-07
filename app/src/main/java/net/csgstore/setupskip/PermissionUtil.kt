package net.csgstore.setupskip

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * Returns true if the current app has Device Admin permissions active
 */

fun Context.isAdminActive(adminReceiver: Class<out DeviceAdminReceiver>): Boolean =
    checkIfAdminActive(devicePolicyManager, ComponentName(this, adminReceiver))

val Context.isThisDeviceOwner: Boolean get() = checkIfDeviceOwner(devicePolicyManager, packageName)

fun Activity.showActivateAdminPermissionRequest(adminReceiver: Class<out DeviceAdminReceiver>) {
    val mDeviceAdmin = ComponentName(packageName, adminReceiver.name)
    doActivateAdminPermissionRequest(mDeviceAdmin)
    isAdminActive(adminReceiver)
}

internal fun Activity.doActivateAdminPermissionRequest(mDeviceAdmin: ComponentName, reason: String? = null) {
    Log.d(TAG, "Sending user to device admin settings page")
    val adminActivateIntent =
        Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin)
    val deviceAdminToast = "Please enable Device Admin.${if (reason != null) "\n$reason" else ""}"
    val alert = this.permissionDialog(adminActivateIntent, deviceAdminToast)
    alert.show()
}

val Context.isWriteSettingsPermissionGranted: Boolean
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(this)

fun DevicePolicyManager.isWriteSecureSettingsPermissionGranted(mDeviceAdmin: ComponentName): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.M || getPermissionGrantState(mDeviceAdmin, PACKAGE_NAME,
        Manifest.permission.WRITE_SECURE_SETTINGS) == 1


internal fun checkIfAdminActive(dpm: DevicePolicyManager, mDeviceAdmin: ComponentName): Boolean {
    return run {
        val state = dpm.isAdminActive(mDeviceAdmin)
        Log.d(ALL_TAG, "Administrator active?? $state")
        state
    }
}

@RequiresApi(VERSION_CODES.JELLY_BEAN_MR2)
private fun checkIfDeviceOwner(dpm: DevicePolicyManager, packageName: String): Boolean {
    return run {
        val state = dpm.isDeviceOwnerApp(packageName)
        Log.d(ALL_TAG, "Administrator active?? $state")
        state
    }
}

fun Activity.permissionDialog(intent: Intent, toast: String): AlertDialog.Builder {
    //    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_ANIMATION
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    return createDialog(title = "Permission Required", message = toast, cancelable = false, onPositiveButton = { dialog, _ ->
        startActivity(intent)
        dialog.dismiss()
    })
}
