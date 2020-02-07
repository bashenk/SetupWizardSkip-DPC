package net.csgstore.setupskip

import android.app.Activity
import android.os.Bundle


class Reset : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val task = AdminReceiver.getPostProvisioningTask(this)
        if (!this.isAdminActive()) {
            this.showActivateAdminPermissionRequest("Required in order to factory reset")
            return
        }
//        sendBroadcast(Intent(AdminReceiver.INTENT_FACTORY_RESET).setPackage(PACKAGE_NAME).setComponent(ComponentName(this, AdminReceiver::class.java)))
        task.showWipeDataPrompt()
//        finish()
    }

    companion object {
        var shortcutCreated = false
        const val ACTION_ADD_SHORTCUT = "net.csgstore.setupskip.Reset.ADD_SHORTCUT"
        const val REQ_CODE_SHORTCUT_ADDED_CALLBACK = 135
    }
}