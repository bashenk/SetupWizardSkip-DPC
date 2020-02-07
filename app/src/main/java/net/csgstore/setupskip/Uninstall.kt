package net.csgstore.setupskip;

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.Toast

class Uninstall : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        val task = AdminReceiver.getPostProvisioningTask(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            task.autoGrantRequestedPermissionsToSelf()
        }
//        if (this.isThisDeviceOwner) task.removeDeviceAdminFromSelf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val packageName = PACKAGE_NAME
            val pUtils = PackageInstallationUtils(this)
            pUtils.uninstallPackage(packageName)
            showToast("Uninstalling package $packageName", Toast.LENGTH_LONG)
        } else {
            showToast("Android version not high enough to perform this action", Toast.LENGTH_LONG)
        }
        task.uninstallPackage(PACKAGE_NAME)
        //        if (this.isThisDeviceOwner) sendBroadcast(Intent(AdminReceiver.INTENT_REMOVE_DEVICE_ADMIN).setPackage(PACKAGE_NAME).setComponent(ComponentName(this, PublicReceiver::class.java)))
//        sendBroadcast(Intent(AdminReceiver.INTENT_UNINSTALL).setPackage(PACKAGE_NAME).setComponent(
//            ComponentName(this, PublicReceiver::class.java)))
        finish()
    }
    companion object {
        var shortcutCreated = false
        const val ACTION_ADD_SHORTCUT = "net.csgstore.setupskip.Uninstall.ADD_SHORTCUT"
        const val REQ_CODE_SHORTCUT_ADDED_CALLBACK = 133
    }
}
