package net.csgstore.setupskip.cosu

import android.Manifest
import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import androidx.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import net.csgstore.setupskip.AdminReceiver
import net.csgstore.setupskip.R
import net.csgstore.setupskip.activityManager
import net.csgstore.setupskip.showToast

fun Any.LogE(log: String) = Log.e(this::class.java.simpleName, log)
fun Any.LogD(log: String) = Log.d(this::class.java.simpleName, log)

class KioskModeActivity : AppCompatActivity() {

    private val dpm: DevicePolicyManager by lazy { getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager }
    private val kioskAppLaunchIntent: Intent? by lazy { packageManager.getLaunchIntentForPackage(KIOSK_APP_PACKAGE_NAME) }
    private val mDeviceAdmin: ComponentName by lazy { AdminReceiver.componentName }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        dismissLockscreen()
        super.onCreate(savedInstanceState)
        KIOSK_APP_PACKAGE_NAME = applicationContext.getString(R.string.kiosk_app_package_name)
        if (kioskAppLaunchIntent == null) {
            showToast("Specified activity was not found.")
            exitKiosk()
            finish()
            return
        }

        if (!dpm.isDeviceOwnerApp(packageName)) {
            LogE("Application is not the device owner, cannot set up COSU device.")
            showToast("Application is not the device owner, cannot set up COSU device.")
            exitKiosk()
            finish()
            return
        }
    }

    private fun isInLockTaskMode(): Boolean {
        val am: ActivityManager? = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am?.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
        } else {
            @Suppress("DEPRECATION")
            am?.isInLockTaskMode!!
        }
    }

    override fun onResume() {
        //        kotlin.runCatching {
        //            disableKeyguard()
        //            dismissLockscreen()
        //        }
        super.onResume()
        isRunning = true
        lockingWorkflow()
    }

    override fun onPause() {
        super.onPause()
        isRunning = false
    }

    private fun lockingWorkflow() {
        performLockTask()
        lockdown()
        AdminReceiver.launchKiosk(this, kioskAppLaunchIntent!!)
    }

    private fun lockdown() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (prefs.getBoolean(PREF_RESTRICTIONS_PROVISIONED_PREF, false)) {
            return
        }
        setPersistentPreferredActivities(this, mDeviceAdmin, true, componentName)

        val context = this
        val active = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            grantPermissionToApp(this, mDeviceAdmin, packageName, Manifest.permission.WRITE_SETTINGS)
            grantPermissionToApp(this, mDeviceAdmin, packageName, Manifest.permission.WRITE_SECURE_SETTINGS)
            try {
                grantPermissionToApp(this, mDeviceAdmin, packageName, Manifest.permission.BIND_DEVICE_ADMIN)
                grantPermissionToApp(this, mDeviceAdmin, packageName, "android.permission.MANAGE_DEVICE_ADMINS")
            } catch (e: Exception) {
                LogD("Meh, we probably didn't need these permissions anyway.")
            }
        }
        val userRestrictions = arrayListOf(
            UserManager.DISALLOW_FACTORY_RESET,
            UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,
            UserManager.DISALLOW_APPS_CONTROL,
            UserManager.DISALLOW_ADD_USER
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            userRestrictions.add(UserManager.DISALLOW_SAFE_BOOT)
            setStatusBarDisabled(context, mDeviceAdmin, active)
        }
        userRestrictions.forEach { dpm.addUserRestriction(mDeviceAdmin, it) }
        val pluggedInto = BatteryManager.BATTERY_PLUGGED_AC or
                BatteryManager.BATTERY_PLUGGED_USB or
                BatteryManager.BATTERY_PLUGGED_WIRELESS
        try {
            Settings.Global.putInt(this.contentResolver, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, pluggedInto)
        } catch (e: SecurityException) {
            LogD("Well, this is awkward.")
        }
        disableKeyguard()

        prefs.edit().putBoolean(PREF_RESTRICTIONS_PROVISIONED_PREF, true).apply()
    }

    fun disableKeyguard() {
        dpm.setKeyguardDisabledFeatures(mDeviceAdmin, DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_ALL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setKeyguardDisabled(this, mDeviceAdmin, true)
        }
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Settings.Secure.putInt(this.contentResolver, Settings.Secure.LOCK_PATTERN_ENABLED, 0)
                Settings.Secure.putInt(this.contentResolver, Settings.Secure.LOCK_PATTERN_VISIBLE, 0)
            } catch (e: SecurityException) {
                LogD("Well, this is awkward.")
            }
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION") val wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
                or PowerManager.ACQUIRE_CAUSES_WAKEUP
                or PowerManager.ON_AFTER_RELEASE,
            "bootlauncher:keyguardwakelock")
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        @Suppress("DEPRECATION") val keyguardLock = keyguardManager.newKeyguardLock("name")
        keyguardLock.disableKeyguard()
    }

    private fun dismissLockscreen() {
        @Suppress("DEPRECATION")
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    private fun setKioskApps(vararg packages: String) {
        if (dpm.isDeviceOwnerApp(packageName)) {
            dpm.setLockTaskPackages(mDeviceAdmin, packages)
        } else {
            LogE("Not device owner!")
            showToast("Not device owner!")
        }
    }

    override fun onBackPressed() {
        // do nothing
        //                super.onBackPressed()
        //        toast("pressed Back")
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_APP_SWITCH || keyCode == KeyEvent.KEYCODE_MENU) {
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    private fun performLockTask() {
        val isDeviceOwner = dpm.isDeviceOwnerApp(packageName)
        val lockTaskPermitted = dpm.isLockTaskPermitted(packageName)
        val inLockTaskMode = isInLockTaskMode()
        if (isDeviceOwner) {
            setKioskApps(packageName, KIOSK_APP_PACKAGE_NAME)
            if (lockTaskPermitted) {
                if (!inLockTaskMode) {
                    activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_NO_USER_ACTION)
                    startLockTask()
                }
            } else {
                showToast("LockTask is not permitted.")
            }
        } else {
            showToast("App is not set as Device Owner")
        }
    }

    override fun setShowWhenLocked(showWhenLocked: Boolean) {
        super.setShowWhenLocked(true)
    }

    private fun exitKiosk() {
        if (dpm.isDeviceOwnerApp(packageName)) {
            dpm.clearPackagePersistentPreferredActivities(mDeviceAdmin, packageName)
            dpm.clearPackagePersistentPreferredActivities(mDeviceAdmin, KIOSK_APP_PACKAGE_NAME)
        }
        if (isInLockTaskMode()) stopLockTask()
        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
            .putBoolean(PREF_RESTRICTIONS_PROVISIONED_PREF, false).apply()
    }

    /**
     * Sets persistent preferred activities.
     *
     * @param context Application Context of the device-owner application calling to set the setting
     * @param mAdminComponentName ComponentName of the DeviceAdminReceiver of the device-owner application calling to set the setting
     * @param enabled    whether they're to be enabled or disabled
     * @param activities the activities
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun setPersistentPreferredActivities(context: Context, mAdminComponentName: ComponentName, enabled: Boolean,
                                         vararg activities: ComponentName
    ) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val intentFilter = IntentFilter(Intent.ACTION_MAIN)
        intentFilter.addCategory(Intent.CATEGORY_HOME)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        for (activity in activities) {
            if (enabled) {
                // set dedicated device activity as home intent receiver so that it is started on reboot
                dpm.addPersistentPreferredActivity(mAdminComponentName, intentFilter, activity)
            } else {
                dpm.clearPackagePersistentPreferredActivities(mAdminComponentName, activity.packageName)
            }
        }
    }

    /**
     * DevicePolicyManager devicePolicyManager =
     * (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
     * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
     * DPCSetupUtils.grantPermissionToApp(this, dpm, context.getPackageName(),
     * Manifest.permission.WRITE_EXTERNAL_STORAGE);
     * }
     *
     * @param context Application Context of the device-owner application calling to set the permission
     * @param mAdminComponentName ComponentName of the DeviceAdminReceiver of the device-owner application calling to set the permission
     * @param appPackageName PackageName String of the app to which the permission will be applied
     * @param manifestPermission String of the manifest permission to be applied
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun grantPermissionToApp(context: Context, mAdminComponentName: ComponentName,
                             appPackageName: String, manifestPermission: String) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        dpm.setPermissionGrantState(mAdminComponentName, appPackageName, manifestPermission,
            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)
        Log.i("DevicePermission", "Permission " + manifestPermission
                + " granted to app " + appPackageName + ".")
    }

    /**
     * Sets status bar disabled.
     *
     * @param context Application Context of the device-owner application calling to set the setting
     * @param mAdminComponentName ComponentName of the DeviceAdminReceiver of the device-owner application calling to set the setting
     * @param disabled the disabled
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setStatusBarDisabled(context: Context, mAdminComponentName: ComponentName, disabled: Boolean) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        dpm.setStatusBarDisabled(mAdminComponentName, disabled)
    }

    /**
     * Sets keyguard disabled.
     *
     * @param context Application Context of the device-owner application calling to set the setting
     * @param mAdminComponentName ComponentName of the DeviceAdminReceiver of the device-owner application calling to set the setting
     * @param disabled the disabled
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setKeyguardDisabled(context: Context, mAdminComponentName: ComponentName, disabled: Boolean) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        dpm.setKeyguardDisabled(mAdminComponentName, disabled)
    }

    companion object {
        lateinit var KIOSK_APP_PACKAGE_NAME: String
        @Volatile
        var isRunning: Boolean = false
        const val PREF_RESTRICTIONS_PROVISIONED_PREF = "pref_restrictions__provisioned"

    }

}