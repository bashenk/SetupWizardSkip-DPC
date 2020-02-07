package net.csgstore.setupskip

import android.app.Activity
import android.os.Bundle

class Unlock : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        val task = AdminReceiver.getPostProvisioningTask(this)
        if (!this.isThisDeviceOwner) {
            this.showToast("App is already not device owner.")
            finish()
            return
        }
        task.removeDeviceAdminFromSelf()
//        sendBroadcast(Intent(AdminReceiver.INTENT_REMOVE_DEVICE_ADMIN).setPackage(PACKAGE_NAME).setComponent(ComponentName(this, PublicReceiver::class.java)))
//        sendBroadcast(Intent(AdminReceiver.INTENT_UNINSTALL).setPackage(PACKAGE_NAME).setComponent(ComponentName(this, PublicReceiver::class.java)))
        finish()
    }

    companion object {
        var shortcutCreated = false
        const val ACTION_ADD_SHORTCUT = "net.csgstore.setupskip.Unlock.ADD_SHORTCUT"
        const val REQ_CODE_SHORTCUT_ADDED_CALLBACK = 131
    }
}