package net.csgstore.setupskip

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle

class Reset : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        val adminReceiver: Class<AdminReceiver> = AdminReceiver::class.java
        if (!this.isAdminActive(adminReceiver)) {
            this.showActivateAdminPermissionRequest(adminReceiver)
            return
        }
        val intent = Intent(AdminReceiver.INTENT_FACTORY_RESET)
            .setPackage(PACKAGE_NAME)
            .setComponent(ComponentName(this, AdminReceiver::class.java))
        sendBroadcast(intent)
        finish()
    }
}