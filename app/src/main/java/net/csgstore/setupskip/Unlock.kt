package net.csgstore.setupskip

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

class Unlock : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        if (!this.isThisDeviceOwner) {
            Toast.makeText(this, "App is not device owner.", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(AdminReceiver.INTENT_REMOVE_DEVICE_ADMIN)
            .setPackage(PACKAGE_NAME)
            .setComponent(ComponentName(this, PublicReceiver::class.java))
        sendBroadcast(intent)
        finish()
    }
}