package net.csgstore.setupskip

import android.app.Activity
import android.app.DownloadManager
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.*
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import net.csgstore.setupskip.cosu.CosuConfig
import net.csgstore.setupskip.cosu.CosuUtils
import net.csgstore.setupskip.cosu.CosuUtils.startDownload
import net.csgstore.setupskip.cosu.KioskModeActivity
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * This activity is started after provisioning is complete in [DeviceAdminReceiver] for
 * COSU devices. It loads a config file and downloads, install, hides and enables apps according
 * to the data in the config file.
 */
class EnableCosuActivity : Activity() {
    private var mDownloadManager: DownloadManager? = null
    private var mConfigDownloadId: Long? = null
    private var mConfig: CosuConfig? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // verify device owner status
        val dpm = getSystemService(
            Context.DEVICE_POLICY_SERVICE
        ) as DevicePolicyManager
        if (!dpm.isDeviceOwnerApp(packageName)) {
            Log.e(CosuUtils.TAG, "HotspotKiosk is not the device owner, cannot set up device.")
            finish()
            return
        }

        // read the admin bundle
        val persistableBundle =
            intent.getParcelableExtra<PersistableBundle>(
                DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE
            )
        if (persistableBundle == null) {
            Log.e(CosuUtils.TAG, "No admin extra bundle")
            finish()
            return
        }
        mDownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // register the download and install receiver
        registerReceiver(mDownloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        registerReceiver(mInstallReceiver, IntentFilter(PackageInstallationUtils.ACTION_INSTALL_COMPLETE))

        // download the config file
        val configDownloadLocation = persistableBundle[BUNDLE_KEY_COSU_CONFIG] as String?
        if (configDownloadLocation == null) {
            Log.e(CosuUtils.TAG, "No download-location specified")
            finishWithFailure()
            return
        }
        if (CosuUtils.DEBUG) Log.d(CosuUtils.TAG, "Downloading config file")
        mConfigDownloadId = startDownload(
            mDownloadManager!!, mHandler,
            configDownloadLocation
        )
    }

    private fun onConfigFileDownloaded() {
        if (CosuUtils.DEBUG) Log.d(CosuUtils.TAG, "Config file downloaded")
        val pfd: ParcelFileDescriptor
        pfd = try {
            mDownloadManager!!.openDownloadedFile(mConfigDownloadId!!)
        } catch (e: FileNotFoundException) {
            Log.e(CosuUtils.TAG, "Download file not found.", e)
            finishWithFailure()
            return
        }
        val `in`: InputStream = FileInputStream(pfd.fileDescriptor)
        mConfig = CosuConfig.createConfig(this, `in`)
        if (mConfig == null) {
            finishWithFailure()
            return
        }
        Log.d(CosuUtils.TAG, "CosuConfig:")
        Log.d(CosuUtils.TAG, mConfig.toString())
        if (!mConfig!!.applyPolicies(AdminReceiver.componentName)
        ) {
            finishWithFailure()
            return
        }
        mConfig!!.initiateDownloadAndInstall(mHandler)
        if (mConfig!!.areAllInstallsFinished()) {
            startCosuMode()
        }
    }

    /**
     * Start the actual COSU mode and drop the user into different screens depending on the value
     * of mode.
     * default: Launch the home screen with a default launcher
     * custom: Launch KioskModeActivity (custom launcher) with only the kiosk apps present
     * single: Launch the first of the kiosk apps
     */
    private fun startCosuMode() {
        var launchIntent: Intent? = null
        var mode = mConfig!!.mode
        val kioskApps = mConfig!!.kioskApps
        if (CosuUtils.DEBUG) Log.d(CosuUtils.TAG, "Starting Cosu mode: $mode")
        if (mode == null) {
            mode = MODE_DEFAULT
        }
        if (MODE_CUSTOM == mode) {
            // Start the KioskModeActivity with all apps in kioskApps
            launchIntent = Intent(this, KioskModeActivity::class.java)
            launchIntent.putExtra(KioskModeActivity.KIOSK_APP_PACKAGE_NAME, kioskApps)
            packageManager.setComponentEnabledSetting(
                ComponentName(packageName, KioskModeActivity::class.java.name),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } else if (MODE_SINGLE == mode) {
            // Start the first app in kioskApps
            if (kioskApps.size != 0) {
                if (CosuUtils.DEBUG) Log.d(CosuUtils.TAG, "  Launching app " + kioskApps[0])
                launchIntent = packageManager.getLaunchIntentForPackage(kioskApps[0])
            }
        } else { // MODE_DEFAULT
            // Start the default launcher with a home intent
            launchIntent = Intent(Intent.ACTION_MAIN)
            launchIntent.addCategory(Intent.CATEGORY_HOME)
        }
        if (launchIntent == null) {
            Log.e(CosuUtils.TAG, "No launch intent specified. Mode=$mode")
            finishWithFailure()
            return
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
        Toast.makeText(this,"Hotspot kiosk successfully set up", Toast.LENGTH_LONG).show()
        unregisterReceiver(mInstallReceiver)
        unregisterReceiver(mDownloadReceiver)

        // check that no timeout messages remain on the handler and remove them
        if (mHandler.hasMessages(CosuUtils.MSG_DOWNLOAD_TIMEOUT)) {
            Log.w(
                CosuUtils.TAG,
                "Download timeout messages remaining on handler thread."
            )
            mHandler.removeMessages(CosuUtils.MSG_DOWNLOAD_TIMEOUT)
        }
        finish()
    }

    private fun finishWithFailure() {
        Toast.makeText(this, "Error during hotspot kiosk set up", Toast.LENGTH_LONG).show()
        unregisterReceiver(mInstallReceiver)
        unregisterReceiver(mDownloadReceiver)
        mHandler.removeMessages(CosuUtils.MSG_DOWNLOAD_TIMEOUT)
        finish()
    }

    private val mHandler: Handler = MessageHandler(this)

    private val mDownloadReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE != intent.action) {
                return
            }
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
            if (CosuUtils.DEBUG) Log.d(
                CosuUtils.TAG,
                "Download complete with id: $id"
            )
            mHandler.sendMessage(mHandler.obtainMessage(CosuUtils.MSG_DOWNLOAD_COMPLETE, id))
        }
    }
    private val mInstallReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            if (PackageInstallationUtils.ACTION_INSTALL_COMPLETE != intent.action) {
                return
            }
            val result = intent.getIntExtra(
                PackageInstaller.EXTRA_STATUS,
                PackageInstaller.STATUS_FAILURE
            )
            val packageName =
                intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
            if (CosuUtils.DEBUG) Log.d(
                CosuUtils.TAG, "PackageInstallerCallback: result=" + result
                        + " packageName=" + packageName
            )
            when (result) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {

                    // this should not happen in M, but will happen in L and L-MR1
                    startActivity(intent.getParcelableExtra<Parcelable>(Intent.EXTRA_INTENT) as Intent)
                }
                PackageInstaller.STATUS_SUCCESS             -> {
                    mHandler.sendMessage(
                        mHandler.obtainMessage(
                            CosuUtils.MSG_INSTALL_COMPLETE,
                            packageName
                        )
                    )
                }
                else                                        -> {
                    Log.e(CosuUtils.TAG, "Install failed.")
                    finishWithFailure()
                    return
                }
            }
        }
    }

    companion object {
        const val BUNDLE_KEY_COSU_CONFIG = "cosu-demo-config-location"
        private const val MODE_CUSTOM = "custom"
        private const val MODE_DEFAULT = "default"
        private const val MODE_SINGLE = "single"

        class MessageHandler(private val enableCosuActivity: EnableCosuActivity) : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    CosuUtils.MSG_DOWNLOAD_COMPLETE -> {
                        if (enableCosuActivity.mConfigDownloadId == msg.obj) {
                            enableCosuActivity.onConfigFileDownloaded()
                            removeMessages(CosuUtils.MSG_DOWNLOAD_TIMEOUT, enableCosuActivity.mConfigDownloadId
                            )
                        } else {
                            val id = enableCosuActivity.mConfig!!.onDownloadComplete(msg.obj as Long)
                            if (id != null) {
                                removeMessages(CosuUtils.MSG_DOWNLOAD_TIMEOUT, id)
                            }
                            if (enableCosuActivity.mConfig!!.areAllInstallsFinished()) {
                                enableCosuActivity.startCosuMode()
                            }
                        }
                    }
                    CosuUtils.MSG_DOWNLOAD_TIMEOUT  -> {
                        val id = (msg.obj as Long).toLong()
                        if (id == enableCosuActivity.mConfigDownloadId) {
                            Log.e(
                                CosuUtils.TAG,
                                "Time out during download of config file"
                            )
                            enableCosuActivity.mDownloadManager!!.remove(enableCosuActivity.mConfigDownloadId!!)
                        } else {
                            enableCosuActivity.mDownloadManager!!.remove(id)
                            Log.e(
                                CosuUtils.TAG,
                                "Time out during app download with id: $id"
                            )
                        }
                        enableCosuActivity.finishWithFailure()
                    }
                    CosuUtils.MSG_INSTALL_COMPLETE  -> {
                        enableCosuActivity.mConfig!!.onInstallComplete(msg.obj as String)
                        if (enableCosuActivity.mConfig!!.areAllInstallsFinished()) {
                            enableCosuActivity.startCosuMode()
                        }
                    }
                }
            }
        }
    }
}