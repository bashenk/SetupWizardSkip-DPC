package net.csgstore.setupskip.cosu


import android.annotation.TargetApi
import android.app.DownloadManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build.VERSION_CODES
import android.os.Handler
import android.util.Log
import android.util.Xml
import net.csgstore.setupskip.common.PackageInstallationUtils
import net.csgstore.setupskip.common.Util
import net.csgstore.setupskip.cosu.CosuUtils.startDownload
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * This class represents the specific cosu set up we want to achieve. The set up is read from an
 * XML config file. Additional apps are downloaded and the specified policies are applied.
 */
/* package */
internal class CosuConfig private constructor(
    private val mContext: Context, inputStream: InputStream
) {
    private val mDownloadManager: DownloadManager =
        mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val mHideApps: MutableSet<String> = HashSet()
    private val mEnableSystemApps: MutableSet<String> = HashSet()
    private val mKioskApps: MutableSet<String> = HashSet()
    private val mDownloadApps: MutableSet<DownloadAppInfo> = HashSet()
    var mode: String? = null
    private val mUserRestrictions: MutableSet<String> = HashSet()
    private val mGlobalSettings: MutableSet<GlobalSetting> = HashSet()
    private var mDisableStatusBar = false
    private var mDisableKeyguard = false
    private var mDisableScreenCapture = false
    private var mDisableCamera = false
    fun applyPolicies(admin: ComponentName): Boolean {
        val dpm = mContext.getSystemService(
            Context.DEVICE_POLICY_SERVICE
        ) as DevicePolicyManager
        try {
            dpm.setLockTaskPackages(admin, kioskApps)
        } catch (e: SecurityException) {
            Log.d(CosuUtils.TAG, "Exception when setting lock task packages", e)
            return false
        }

        // hide apps
        for (pkg in mHideApps) {
            dpm.setApplicationHidden(admin, pkg, true)
        }

        // enable system apps
        for (pkg in mEnableSystemApps) {
            try {
                dpm.enableSystemApp(admin, pkg)
            } catch (e: IllegalArgumentException) {
                Log.w(
                    CosuUtils.TAG,
                    "Failed to enable $pkg. Operation is only allowed for system apps."
                )
            }
        }

        // set user restrictions
        for (userRestriction in mUserRestrictions) {
            dpm.addUserRestriction(admin, userRestriction)
        }
        for (globalSetting in mGlobalSettings) {
            dpm.setGlobalSetting(admin, globalSetting.key, globalSetting.value)
        }
        if (Util.SDK_INT >= VERSION_CODES.M) {
            disableKeyGuardAndStatusBar(dpm, admin)
        }
        dpm.setScreenCaptureDisabled(admin, mDisableScreenCapture)
        dpm.setCameraDisabled(admin, mDisableCamera)
        return true
    }

    @TargetApi(VERSION_CODES.M)
    private fun disableKeyGuardAndStatusBar(dpm: DevicePolicyManager, admin: ComponentName) {
        dpm.setStatusBarDisabled(admin, mDisableStatusBar)
        dpm.setKeyguardDisabled(admin, mDisableKeyguard)
    }

    fun initiateDownloadAndInstall(handler: Handler?) {
        for (ai in mDownloadApps) {
            ai.downloadId = startDownload(mDownloadManager, handler!!, ai.downloadLocation)
        }
    }

    val kioskApps: Array<String>
        get() = mKioskApps.toTypedArray()

    fun areAllInstallsFinished(): Boolean {
        for (ai in mDownloadApps) {
            if (!ai.installCompleted) {
                return false
            }
        }
        return true
    }

    fun onDownloadComplete(id: Long): Long? {
        for (ai in mDownloadApps) {
            if (id == ai.downloadId) {
                if (CosuUtils.DEBUG) Log.d(
                    CosuUtils.TAG, "Package download complete: " + ai.packageName
                )
                ai.downloadCompleted = true
                try {
                    val pfd = mDownloadManager.openDownloadedFile(id)
                    val `in`: InputStream = FileInputStream(pfd.fileDescriptor)
                    PackageInstallationUtils.installPackage(mContext, `in`, ai.packageName)
                } catch (e: IOException) {
                    Log.e(
                        CosuUtils.TAG, "Error installing package: ${ai.packageName}", e
                    )
                    // We are still marking the package as "installed", just so we don't block the
                    // entire flow.
                    ai.installCompleted = true
                }
                return ai.downloadId
            }
        }
        Log.w(CosuUtils.TAG, "Unknown download id: $id")
        return null
    }

    fun onInstallComplete(packageName: String) {
        if (CosuUtils.DEBUG) Log.d(
            CosuUtils.TAG, "Package install complete: $packageName"
        )
        for (ai in mDownloadApps) {
            if (packageName == ai.packageName) {
                ai.installCompleted = true
                return
            }
        }
    }

    /**
     * Read a number of apps from the xml parser
     */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readApps(
        parser: XmlPullParser, apps: MutableSet<String>
    ) {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (TAG_APP == name) {
                val packageName = parser.getAttributeValue(null, ATTRIBUTE_PACKAGE_NAME)
                if (packageName != null) {
                    apps.add(packageName)
                }
                skipCurrentTag(parser)
            }
        }
    }

    /**
     * Read a number of apps with download information from the xml parser
     */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readDownloadApps(
        parser: XmlPullParser, apps: MutableSet<DownloadAppInfo>
    ) {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (TAG_APP == name) {
                val packageName = parser.getAttributeValue(null, ATTRIBUTE_PACKAGE_NAME)
                val downloadLocation = parser.getAttributeValue(
                    null, ATTRIBUTE_DOWNLOAD_LOCATION
                )
                if (packageName != null && downloadLocation != null) {
                    apps.add(DownloadAppInfo(packageName, downloadLocation))
                }
                skipCurrentTag(parser)
            }
        }
    }

    /**
     * Read the policies to be set
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readPolicies(parser: XmlPullParser) {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (TAG_USER_RESTRICTION == name) {
                val userRestriction = parser.getAttributeValue(null, ATTRIBUTE_NAME)
                if (userRestriction != null) {
                    mUserRestrictions.add(userRestriction)
                }
            } else if (TAG_GLOBAL_SETTING == name) {
                val setting = parser.getAttributeValue(null, ATTRIBUTE_NAME)
                val value = parser.getAttributeValue(null, ATTRIBUTE_VALUE)
                if (setting != null && value != null) {
                    mGlobalSettings.add(GlobalSetting(setting, value))
                }
            } else if (TAG_DISABLE_STATUS_BAR == name) {
                mDisableStatusBar = java.lang.Boolean.parseBoolean(
                    parser.getAttributeValue(
                        null, ATTRIBUTE_VALUE
                    )
                )
            } else if (TAG_DISABLE_KEYGUARD == name) {
                mDisableKeyguard = java.lang.Boolean.parseBoolean(
                    parser.getAttributeValue(
                        null, ATTRIBUTE_VALUE
                    )
                )
            } else if (TAG_DISABLE_CAMERA == name) {
                mDisableCamera = java.lang.Boolean.parseBoolean(
                    parser.getAttributeValue(
                        null, ATTRIBUTE_VALUE
                    )
                )
            } else if (TAG_DISABLE_SCREEN_CAPTURE == name) {
                mDisableScreenCapture = java.lang.Boolean.parseBoolean(
                    parser.getAttributeValue(
                        null, ATTRIBUTE_VALUE
                    )
                )
            }
            skipCurrentTag(parser)
        }
    }

    /**
     * Continue to the end of the current xml tag
     */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun skipCurrentTag(parser: XmlPullParser) {
        val outerDepth = parser.depth
        var type: Int
        while (parser.next().also {
                type = it
            } != XmlPullParser.END_DOCUMENT && (type != XmlPullParser.END_TAG || parser.depth > outerDepth)) {
        }
    }

    private class DownloadAppInfo(
        val packageName: String, val downloadLocation: String
    ) {
        var downloadId: Long? = null
        var downloadCompleted = false
        var installCompleted = false
        override fun toString(): String {
            return ("packageName: " + packageName + " downloadLocation: " + downloadLocation)
        }

    }

    private class GlobalSetting(val key: String, val value: String) {
        override fun toString(): String {
            return "setting: $key value: $value"
        }

    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("Mode: ").append(mode).append(NEW_LINE)
        builder.append("Disable status bar: ").append(mDisableStatusBar).append(NEW_LINE)
        builder.append("Disable keyguard: ").append(mDisableKeyguard).append(NEW_LINE)
        builder.append("Disable screen capture: ").append(mDisableScreenCapture).append(NEW_LINE)
        builder.append("Disable camera: ").append(mDisableCamera).append(NEW_LINE)
        builder.append("User restrictions:").append(NEW_LINE)
        dumpSet(builder, mUserRestrictions)
        builder.append("Global settings:").append(NEW_LINE)
        dumpSet(builder, mGlobalSettings)
        builder.append("Hide apps:").append(NEW_LINE)
        dumpSet(builder, mHideApps)
        builder.append("Enable system apps:").append(NEW_LINE)
        dumpSet(builder, mEnableSystemApps)
        builder.append("Kiosk apps:").append(NEW_LINE)
        dumpSet(builder, mKioskApps)
        builder.append("Download apps:").append(NEW_LINE)
        dumpSet(builder, mDownloadApps)
        return builder.toString()
    }

    private fun dumpSet(
        builder: StringBuilder, set: Set<*>
    ) {
        for (obj in set) {
            builder.append("  ").append(obj.toString()).append(NEW_LINE)
        }
    }

    companion object {
        private const val TAG_APP = "app"
        private const val TAG_COSU_CONFIG = "cosu-config"
        private const val TAG_DOWNLOAD_APPS = "download-apps"
        private const val TAG_ENABLE_APPS = "enable-apps"
        private const val TAG_HIDE_APPS = "hide-apps"
        private const val TAG_KIOSK_APPS = "kiosk-apps"
        private const val TAG_POLICIES = "policies"
        private const val TAG_USER_RESTRICTION = "user-restriction"
        private const val TAG_GLOBAL_SETTING = "global-setting"
        private const val TAG_DISABLE_STATUS_BAR = "disable-status-bar"
        private const val TAG_DISABLE_KEYGUARD = "disable-keyguard"
        private const val TAG_DISABLE_CAMERA = "disable-camera"
        private const val TAG_DISABLE_SCREEN_CAPTURE = "disable-screen-capture"
        private const val ATTRIBUTE_DOWNLOAD_LOCATION = "download-location"
        private const val ATTRIBUTE_MODE = "mode"
        private const val ATTRIBUTE_PACKAGE_NAME = "package-name"
        private const val ATTRIBUTE_VALUE = "value"
        private const val ATTRIBUTE_NAME = "name"
        private val NEW_LINE = System.getProperty("line.separator")
        fun createConfig(context: Context, `in`: InputStream): CosuConfig? {
            return try {
                CosuConfig(context, `in`)
            } catch (e: XmlPullParserException) {
                Log.e(CosuUtils.TAG, "Exception during config creation.", e)
                null
            } catch (e: IOException) {
                Log.e(CosuUtils.TAG, "Exception during config creation.", e)
                null
            }
        }
    }

    /**
     * Parses the config xml file given in the form of an InputStream.
     */
    init {
        try {
            val parser = Xml.newPullParser()
            parser.setInput(inputStream, null)
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                val name = parser.name
                if (TAG_COSU_CONFIG == name) {
                    mode = parser.getAttributeValue(null, ATTRIBUTE_MODE)
                } else if (TAG_POLICIES == name) {
                    readPolicies(parser)
                } else if (TAG_ENABLE_APPS == name) {
                    readApps(parser, mEnableSystemApps)
                } else if (TAG_HIDE_APPS == name) {
                    readApps(parser, mHideApps)
                } else if (TAG_KIOSK_APPS == name) {
                    readApps(parser, mKioskApps)
                } else if (TAG_DOWNLOAD_APPS == name) {
                    readDownloadApps(parser, mDownloadApps)
                }
            }
        } finally {
            inputStream.close()
        }
    }
}