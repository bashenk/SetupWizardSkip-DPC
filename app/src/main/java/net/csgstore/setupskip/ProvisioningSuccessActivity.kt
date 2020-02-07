package net.csgstore.setupskip

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast


/**
 * Activity that gets launched by the
 * [android.app.admin.DevicePolicyManager.ACTION_PROVISIONING_SUCCESSFUL] intent.
 */
class ProvisioningSuccessActivity : Activity() {
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        val task = AdminReceiver.getPostProvisioningTask(this)
        if (!task.performPostProvisioningOperations(intent)) {
            finish()
            return
        }
        val launchIntent: Intent? = task.getPostProvisioningLaunchIntent(intent)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Log.e(Companion.TAG, "ProvisioningSuccessActivity.onCreate() invoked, but ownership not assigned")
            this.showToast("ProvisioningSuccessActivity.onCreate() invoked, but ownership not assigned")
        }
        finish()
    }

    companion object {
        private const val TAG = "ProvisioningSuccess"
    }
}