package net.csgstore.setupskip

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context


object ProvisioningUtil {
    fun enableProfile(context: Context) {
        val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName: ComponentName = AdminReceiver.componentName
        // This is the name for the newly created managed profile.
        manager.setProfileName(componentName, context.getString(R.string.profile_name))
        // We enable the profile here.
        manager.setProfileEnabled(componentName)
    }


}