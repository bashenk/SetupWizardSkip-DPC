package net.csgstore.setupskip

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.*
import android.net.wifi.WifiManager
import android.os.*
import android.os.Build.VERSION_CODES
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.CheckBox
import android.widget.Toast
import java.io.IOException
import java.lang.ref.WeakReference


/**
 * Task executed after provisioning is done indicated by either the
 * [DevicePolicyManager.ACTION_PROVISIONING_SUCCESSFUL] activity intent or the
 * [android.app.admin.DeviceAdminReceiver.onProfileProvisioningComplete]
 * broadcast.
 *
 *
 * Operations performed:
 *
 *  * self-grant all run-time permissions
 *  * enable the launcher activity
 *  * start waiting for first account ready broadcast
 *
 */
class PostProvisioningTask(private val context: WeakReference<Activity>) {
    private val mContext: Activity?
        get() = context.get()
    private val mDevicePolicyManager: DevicePolicyManager by lazy { mContext?.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager }
    private val adminComponentName: ComponentName by lazy { AdminReceiver.componentName }
    private val devicePolicyManager: DevicePolicyManager by lazy { mContext?.devicePolicyManager as DevicePolicyManager }
    private val userManager: UserManager by lazy { mContext?.userManager as UserManager }
    private val mSharedPrefs: SharedPreferences
    private val packageManager by lazy { mContext?.packageManager as PackageManager }
    private lateinit var mGetAccessibilityServicesTask: GetAccessibilityServicesTask

    fun performPostProvisioningOperations(intent: Intent): Boolean {
        if (isPostProvisioningDone) return false
        markPostProvisioningDone()
        // From M onwards, permissions are not auto-granted, so we need to manually grant
        // permissions for TestDPC.
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            autoGrantRequestedPermissionsToSelf()
        }
        setDevicePolicySettings()
        val createProfile: Boolean = intent.extras?.getBoolean(EXTRA_CREATE_PROFILE) ?: false
        if (!createProfile) return false
        val flags = when {
            Build.VERSION.SDK_INT >= VERSION_CODES.P -> DevicePolicyManager.SKIP_SETUP_WIZARD and DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED
            Build.VERSION.SDK_INT >= VERSION_CODES.N -> DevicePolicyManager.SKIP_SETUP_WIZARD
            else -> 0
        }
        val userHandle = when {
            Build.VERSION.SDK_INT >= VERSION_CODES.N -> devicePolicyManager.createAndManageUser(adminComponentName, "Tester",
                adminComponentName, null, flags) ?: Binder.getCallingUserHandle()
            else -> Binder.getCallingUserHandle()
        }
        val serialNumber = userManager.getSerialNumberForUser(userHandle)
        Log.i(ALL_TAG, "Device admin enabled for user with serial number: $serialNumber")


        // Retreive the admin extras bundle, which we can use to determine the original context for
        // TestDPCs launch.
        //        val extras = intent.getParcelableExtra<PersistableBundle>(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE)
        //        if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
        //            maybeSetAffiliationIds(extras)
        //        }
        //        // If TestDPC asked GmsCore to store its state in the FRP area before factory reset, the
        //        // state will be handed over to it during the next device setup.
        //        if (Build.VERSION.SDK_INT >= VERSION_CODES.O_MR1 && extras != null && extras.containsKey(KEY_DEVICE_OWNER_STATE)) {
        //            Util.setPersistentDoStateWithApplicationRestriction(mContext, mDevicePolicyManager, adminComponentName, extras.getString(KEY_DEVICE_OWNER_STATE))
        //        }
        // Hide the setup launcher when this app is the admin
        mContext?.let {
            packageManager.setComponentEnabledSetting(ComponentName(it, TEMPORARY_LAUNCH_ACTIVITY),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }
        return true
    }

    private fun markPostProvisioningDone() = mSharedPrefs.edit().putBoolean(KEY_POST_PROV_DONE, true).apply()

    private val isPostProvisioningDone: Boolean
        get() = mSharedPrefs.getBoolean(KEY_POST_PROV_DONE, false)

    //    @TargetApi(VERSION_CODES.O)
    //    private fun maybeSetAffiliationIds(extras: PersistableBundle?) {
    //        if (extras == null) {
    //            return
    //        }
    //        val affiliationId = extras.getString(LaunchIntentUtil.EXTRA_AFFILIATION_ID)
    //        if (affiliationId != null) {
    //            mDevicePolicyManager.setAffiliationIds(adminComponentName, Collections.singleton(affiliationId))
    //        }
    //    }

    fun getPostProvisioningLaunchIntent(intent: Intent): Intent? { // Enable the profile after provisioning is complete.
        // Retreive the admin extras bundle, which we can use to determine the original context for
        // TestDPCs launch.
        val extras = intent.getParcelableExtra<PersistableBundle>(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE)
        val packageName = mContext?.packageName
        val synchronousAuthLaunch: Boolean = LaunchIntentUtil.isSynchronousAuthLaunch(extras)
        val cosuLaunch: Boolean = LaunchIntentUtil.isCosuLaunch(extras)
        val isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(packageName)
        val isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(packageName)
        // Drop out quickly if we're neither profile or device owner.
        if (!isProfileOwner && !isDeviceOwner) {
            return null
        }
        //        if (cosuLaunch) {
                    val launch = Intent(mContext, EnableCosuActivity::class.java)
                    launch.putExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE, extras)
        //        } else {
//        val launch = Intent(mContext, ShortcutCreator::class.java)
        //        }
        /*        if (synchronousAuthLaunch) {
                    val accountName: String? = LaunchIntentUtil.getAddedAccountName(extras)
                    if (accountName != null) launch.putExtra(LaunchIntentUtil.EXTRA_ACCOUNT_NAME, accountName)
                }*/
        // For synchronous auth cases, we can assume accounts are already setup (or will be shortly,
        // as account migration for Profile Owner is asynchronous). For COSU we don't want to show
        // the account option to the user, as no accounts should be added for now.
        // In other cases, offer to add an account to the newly configured device/profile.
        /*        if (!synchronousAuthLaunch && !cosuLaunch) {
                    val accountManager = AccountManager.get(mContext)
                    val accounts = accountManager.accounts
                    if (accounts.isEmpty()) { // Add account after provisioning is complete.
                        val addAccountIntent = Intent(mContext, AddAccountActivity::class.java)
                        addAccountIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addAccountIntent.putExtra(AddAccountActivity.EXTRA_NEXT_ACTIVITY_INTENT, launch)
                        return addAccountIntent
                    }
                }*/
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return launch
    }

    internal fun configureAccessibility() {
        val list = devicePolicyManager.getPermittedAccessibilityServices(adminComponentName) ?: mutableListOf()
        if (::mGetAccessibilityServicesTask.isInitialized && !mGetAccessibilityServicesTask.isCancelled) {
            mGetAccessibilityServicesTask.cancel(true)
        }
//        mGetAccessibilityServicesTask = GetAccessibilityServicesTask(this)
//        mGetAccessibilityServicesTask.execute()

//        mContext!!.let {
//            val accessibilityService = ComponentName(it, MyAccessibilityService::class.java)
//            list.add(accessibilityService.flattenToString())
//        }
        list.add(PACKAGE_NAME)
        devicePolicyManager.setPermittedAccessibilityServices(adminComponentName, list)
    }

    @TargetApi(VERSION_CODES.M)
    internal fun autoGrantRequestedPermissionsToSelf() {
        mDevicePolicyManager.setPermissionPolicy(adminComponentName, PERMISSION_GRANT_STATE_GRANTED)
        mContext?.let {
            val packageName: String = it.packageName
            val permissions = getRuntimePermissions(packageName)
            for (permission in permissions) {
                val success = mDevicePolicyManager.setPermissionGrantState(adminComponentName, packageName, permission,
                    PERMISSION_GRANT_STATE_GRANTED)
                Log.d(TAG, "Auto-granting $permission, success: $success")
                if (!success) {
                    Log.e(TAG, "Failed to auto grant permission to self: $permission")
                }
            }
        }
    }

    private fun getRuntimePermissions(packageName: String): List<String> {
        val permissions: MutableList<String> = ArrayList()
        val packageInfo: PackageInfo?
        packageInfo = try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(Companion.TAG, "Could not retrieve info about the package: $packageName", e)
            return permissions
        }
        packageInfo?.requestedPermissions?.forEach { requestedPerm ->
            //            if (isRuntimePermission(requestedPerm)) {
            permissions.add(requestedPerm)
            //            }
        }
        return permissions
    }

    private fun isRuntimePermission(permission: String): Boolean {
        try {
            val pInfo = packageManager.getPermissionInfo(permission, 0) ?: return false
            val basePermissionType =
                if (Build.VERSION.SDK_INT >= VERSION_CODES.P) pInfo.protection else pInfo.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE
            val permissionFlags =
                if (Build.VERSION.SDK_INT >= VERSION_CODES.P) pInfo.protectionFlags else pInfo.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE.inv()
            if (basePermissionType == PermissionInfo.PROTECTION_DANGEROUS || basePermissionType == PermissionInfo.PROTECTION_NORMAL) return true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.i(Companion.TAG, "Could not retrieve info about the permission: $permission")
        }
        return false
    }

    private fun setDevicePolicySettings(shouldSetOn: Boolean = true) {
        val setting = if (shouldSetOn) "1" else "0"
        devicePolicyManager.setGlobalSetting(adminComponentName, Settings.Global.ADB_ENABLED, setting)
        devicePolicyManager.setGlobalSetting(adminComponentName, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, setting)
        if (Build.VERSION.SDK_INT < VERSION_CODES.M) {
            // Changing the following settings has no effect as of {@link android.os.Build.VERSION_CODES#M}
            devicePolicyManager.setGlobalSetting(adminComponentName, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, setting)
            devicePolicyManager.setGlobalSetting(adminComponentName, Settings.Global.WIFI_ON, setting)
        } else {
            devicePolicyManager.setKeyguardDisabled(adminComponentName, shouldSetOn)
            if (shouldSetOn) {
                (mContext?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled = true
            }
            val plugInState = if (shouldSetOn) BatteryManager.BATTERY_PLUGGED_AC or BatteryManager.BATTERY_PLUGGED_USB else 0
            // This setting is only available from {@link android.os.Build.VERSION_CODES#M} onwards and can only be set if {@link #setMaximumTimeToLock} is not used to set a timeout.
            devicePolicyManager.setGlobalSetting(adminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                plugInState.toString())
        }
    }

    internal fun removeDeviceAdminFromSelf() {
        val notOwnerOrAdministrator = "Setup Wizard Skip is already not a device owner/administrator"
        mContext?.let {
            if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && it.isThisDeviceOwner) {
                if (OBEY_DEPRECATION && Build.VERSION.SDK_INT >= VERSION_CODES.O) {
                    it.showToast("Skipping because of deprecation.")
                    return
                }
                try {
                    @Suppress("DEPRECATION") it.devicePolicyManager.clearDeviceOwnerApp(PACKAGE_NAME)
                    it.showToast("Successfully disabled device owner for package Setup Wizard Skip")
                } catch (e: Exception) {
                    it.showToast("Setup Wizard Skip is already not a device owner")
                }
            } else if (!it.isThisDeviceOwner && it.isAdminActive(AdminReceiver::class.java)) {
                try {
                    it.devicePolicyManager.removeActiveAdmin(adminComponentName)
                    it.showToast("Successfully disabled device administrator for package Setup Wizard Skip")
                } catch (e: Exception) {
                    it.showToast(notOwnerOrAdministrator)
                }
            } else {
                it.showToast(notOwnerOrAdministrator)
            }
        }
    }

    internal fun uninstallPackage(packageName: String = PACKAGE_NAME): Boolean {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            val packageInstaller = packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            params.setAppPackageName(packageName)
            val sessionId = try {
                packageInstaller.createSession(params)
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            packageInstaller.uninstall(packageName,
                PendingIntent.getBroadcast(mContext, sessionId, Intent("android.intent.action.MAIN"), 0).intentSender)
            mContext?.showToast("Uninstalling package $packageName!", Toast.LENGTH_LONG)
            return true
        }
        System.err.println("old sdk")
        return false
    }

    /**
     * Shows a prompt to ask for confirmation on wiping the data and also provide an option
     * to set if external storage and factory reset protection data also needs to wiped.
     */
    internal fun showWipeDataPrompt() {
        if (mContext !is Activity) return factoryReset()
        mContext?.run {
            val inflater: LayoutInflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.wipe_data_dialog_prompt, null)
            val externalStorageCheckBox = dialogView.findViewById(R.id.external_storage_checkbox) as CheckBox
            val resetProtectionCheckBox = dialogView.findViewById(R.id.reset_protection_checkbox) as CheckBox
            val dialog: AlertDialog.Builder = createDialog(R.string.wipe_data_title, dialogView, true, { _, _ ->
                var flags = 0
                flags = flags or if (externalStorageCheckBox.isChecked) DevicePolicyManager.WIPE_EXTERNAL_STORAGE else 0
                if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1) flags =
                    flags or if (resetProtectionCheckBox.isChecked) DevicePolicyManager.WIPE_RESET_PROTECTION_DATA else 0
                devicePolicyManager.wipeData(flags)
            }, { dialog, _ -> dialog.dismiss() })
            dialog.show()
        }
    }

    private fun factoryReset() {
        mContext?.showToast("Would've done an automated factory reset just now.")
        Log.d(TAG, "Would've done an automated factory reset just now.")
        //        devicePolicyManager.wipeData(mContext.getResetFlags())
    }

    companion object {
        private const val TAG = "PostProvisioningTask"
        private const val TEMPORARY_LAUNCH_ACTIVITY = "$PACKAGE_NAME.ShortcutCreator"
        private const val KIOSK_ACTIVITY = "$PACKAGE_NAME.KioskActivity"
        private const val POST_PROV_PREFS = "post_prov_prefs"
        private const val KEY_POST_PROV_DONE = "key_post_prov_done"
        private const val KEY_DEVICE_OWNER_STATE = "android.app.extra.PERSISTENT_DEVICE_OWNER_STATE"
        private const val EXTRA_CREATE_PROFILE = "key_create_profile"

        const val INTENT_REMOVE_DEVICE_ADMIN = "$PACKAGE_NAME.REMOVE_DEVICE_ADMIN"
        const val INTENT_UNINSTALL = "$PACKAGE_NAME.UNINSTALL"
        const val INTENT_FACTORY_RESET = "$PACKAGE_NAME.FACTORY_RESET"
        private const val OBEY_DEPRECATION: Boolean = false

        /**
         * Gets all the accessibility services. After all the accessibility services are retrieved, the
         * result is displayed in a popup.
         */
        private open class GetAccessibilityServicesTask(private val postProvisioningTask: PostProvisioningTask) : AsyncTask<Void, Void, List<AccessibilityServiceInfo?>>() {
            private val mAccessibilityManager: AccessibilityManager =
                postProvisioningTask.mContext?.getSystemService(
                    Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

            protected override fun doInBackground(vararg voids: Void?): List<AccessibilityServiceInfo> {
                return mAccessibilityManager.installedAccessibilityServiceList
            }

            protected fun getResolveInfoListFromAvailableComponents(
                accessibilityServiceInfoList: List<AccessibilityServiceInfo>
            ): List<ResolveInfo> {
                val packageSet: HashSet<String> = HashSet()
                val resolveInfoList: MutableList<ResolveInfo> = ArrayList()
                for (accessibilityServiceInfo in accessibilityServiceInfoList) {
                    val resolveInfo = accessibilityServiceInfo.resolveInfo
                    // Some apps may contain multiple accessibility services. Make sure that the package
                    // name is unique in the return list.
                    if (!packageSet.contains(resolveInfo.serviceInfo.packageName)) {
                        resolveInfoList.add(resolveInfo)
                        packageSet.add(resolveInfo.serviceInfo.packageName)
                    }
                }
                return resolveInfoList
            }

            protected var permittedComponentsList: List<String?>?
                protected get() = postProvisioningTask.mDevicePolicyManager.getPermittedAccessibilityServices(
                    postProvisioningTask.adminComponentName)
                protected set(permittedAccessibilityServices) {
                    val result: Boolean =
                        postProvisioningTask.mDevicePolicyManager.setPermittedAccessibilityServices(
                            postProvisioningTask.adminComponentName, permittedAccessibilityServices)
                    val successMsgId: Int =
                        if (permittedAccessibilityServices == null) R.string.all_accessibility_services_enabled else R.string.set_accessibility_services_successful
                    postProvisioningTask.mContext?.showToast(if (result) successMsgId else R.string.set_accessibility_services_fail)
                }

        }
    }

    init {
        mSharedPrefs = mContext!!.getSharedPreferences(POST_PROV_PREFS, Context.MODE_PRIVATE)
    }

    /**
     * Bit mask of additional options: currently supported flags are [DevicePolicyManager.WIPE_EXTERNAL_STORAGE] and
     * [DevicePolicyManager.WIPE_RESET_PROTECTION_DATA].
     *
     * Can be one or all of 0, [DevicePolicyManager.WIPE_EXTERNAL_STORAGE], and
     * [DevicePolicyManager.WIPE_RESET_PROTECTION_DATA].
     *
     * Apparently, [DevicePolicyManager.WIPE_EUICC] is as-yet unimplemented.
     *
     * @param  this@getFlags [Context] from which to retrieve the [DevicePolicyManager] and package name.
     * @return [Int]   The bitmask for the most destructive reset action possible with the given permissions.
     */
    @SuppressLint("ObsoleteSdkInt")
    fun Context.getResetFlags(
        wipeResetProtection: Boolean = true, wipeExternalStorage: Boolean = false, wipeEUICC: Boolean = false
    ): Int {
        var integer = 0
        if (wipeExternalStorage && Build.VERSION.SDK_INT > VERSION_CODES.JELLY_BEAN_MR2) {
            integer = integer or DevicePolicyManager.WIPE_EXTERNAL_STORAGE
        }
        if (wipeResetProtection && Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1 && devicePolicyManager.isDeviceOwnerApp(
                packageName)) {
            integer = integer or DevicePolicyManager.WIPE_RESET_PROTECTION_DATA
        }
        if (wipeEUICC && Build.VERSION.SDK_INT >= VERSION_CODES.P) {
            integer = integer or DevicePolicyManager.WIPE_EUICC
        }
        return integer
    }
}