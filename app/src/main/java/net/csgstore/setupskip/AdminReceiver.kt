package net.csgstore.setupskip

import android.annotation.TargetApi
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.PersistableBundle
import android.os.UserManager
import android.util.Log
import androidx.annotation.RequiresApi


class AdminReceiver : DeviceAdminReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED                     -> { }
            REMOVE_DEVICE_ADMIN -> {
                val dpm = context?.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager?
                if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < VERSION_CODES.O) {
                    @Suppress("DEPRECATION")
                    dpm!!.clearDeviceOwnerApp(PACKAGE_NAME)
                }
                if (context != null) {
                    val component = ComponentName(context, AdminReceiver::class.java)
                    dpm!!.removeActiveAdmin(component)
                }
            }
            DevicePolicyManager.ACTION_PROFILE_OWNER_CHANGED -> onProfileOwnerChanged(context)
            DevicePolicyManager.ACTION_DEVICE_OWNER_CHANGED  -> onDeviceOwnerChanged(context)
            else                                             -> context?.let {
                super.onReceive(it, intent)
            }
        }
    }

    @RequiresApi(VERSION_CODES.JELLY_BEAN_MR1)
    override fun onEnabled(context: Context, intent: Intent) {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val flags = when {
            Build.VERSION.SDK_INT >= VERSION_CODES.P -> DevicePolicyManager.SKIP_SETUP_WIZARD and DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED
            Build.VERSION.SDK_INT >= VERSION_CODES.N -> DevicePolicyManager.SKIP_SETUP_WIZARD
            else                                     -> 0
        }
        val userHandle = if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
            dpm.createAndManageUser(componentName, "Tester", componentName, null, flags)
        } else Binder.getCallingUserHandle()
        val serialNumber = userManager.getSerialNumberForUser(userHandle)
        Log.i(TAG, "Device admin enabled in user with serial number: $serialNumber")
        super.onEnabled(context, intent);
    }

    private val componentName: ComponentName
        get() = ComponentName(PACKAGE_NAME, AdminReceiver::class.java.name)

    private fun onProfileOwnerChanged(context: Context?) {
        Log.i(TAG, "onProfileOwnerChanged")
    }

    private fun onDeviceOwnerChanged(context: Context?) {
        Log.i(TAG, "onDeviceOwnerChanged")
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
    }

    @TargetApi(VERSION_CODES.P)
    override fun onTransferOwnershipComplete(
        context: Context,
        bundle: PersistableBundle?
    ) {
        Log.i(TAG, "onTransferOwnershipComplete")
        super.onTransferOwnershipComplete(context, bundle)
    }

    companion object {
        public val REMOVE_DEVICE_ADMIN = "${AdminReceiver::class.java.name}.REMOVE_DEVICE_ADMIN"
    }
}
