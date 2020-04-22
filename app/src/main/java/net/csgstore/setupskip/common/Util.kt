package net.csgstore.setupskip.common

import android.annotation.TargetApi
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.UserHandle
import android.os.UserManager
import android.text.format.DateUtils
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.preference.PreferenceFragment
import net.csgstore.setupskip.AdminReceiver
import net.csgstore.setupskip.R
import net.csgstore.setupskip.devicePolicyManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ /**
 * Common utility functions.
 */
object Util {
    private const val TAG = "Util"
    private const val DEFAULT_BUFFER_SIZE = 4096
    private const val BROADCAST_ACTION_FRP_CONFIG_CHANGED = "com.google.android.gms.auth.FRP_CONFIG_CHANGED"
    private const val GMSCORE_PACKAGE = "com.google.android.gms"
    private const val PERSISTENT_DEVICE_OWNER_STATE = "persistentDeviceOwnerState"

    // TODO:(133143789): Remove when we no longer need to support pre-release Q
    private val IS_RUNNING_Q = VERSION.CODENAME.length == 1 && VERSION.CODENAME[0] == 'Q'
    const val Q_VERSION_CODE = 29

    /**
     * A replacement for [VERSION.SDK_INT] that is compatible with pre-release SDKs
     *
     *
     * This will be set to the version SDK, or [VERSION_CODES.CUR_DEVELOPMENT] if the SDK
     * int is not yet assigned.
     */
    val SDK_INT = if (IS_RUNNING_Q) Q_VERSION_CODE else VERSION.SDK_INT

    /**
     * Format a friendly datetime for the current locale according to device policy documentation.
     * If the timestamp doesn't represent a real date, it will be interpreted as `null`.
     *
     * @return A [CharSequence] such as "12:35 PM today" or "June 15, 2033", or `null`
     * in the case that {@param timestampMs} equals zero.
     */
    fun formatTimestamp(timestampMs: Long): CharSequence? {
        return if (timestampMs == 0L) {
            // DevicePolicyManager documentation describes this timestamp as having no effect,
            // so show nothing for this case as the policy has not been set.
            null
        } else DateUtils.formatSameDayTime(timestampMs, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_WEEKDAY,
            DateUtils.FORMAT_SHOW_TIME)
    }

    fun updateImageView(
        context: Context, imageView: ImageView, uri: Uri?
    ) {
        try {
            var inputStream = context.contentResolver.openInputStream(uri!!)
            // Avoid decoding the entire image if the imageView holding this image is smaller.
            val bounds = BitmapFactory.Options()
            bounds.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, bounds)
            val streamWidth = bounds.outWidth
            val streamHeight = bounds.outHeight
            val maxDesiredWidth = imageView.maxWidth
            val maxDesiredHeight = imageView.maxHeight
            val ratio = Math.max(streamWidth / maxDesiredWidth, streamHeight / maxDesiredHeight)
            if (ratio > 1) {
                bounds.inSampleSize = ratio
            }
            bounds.inJustDecodeBounds = false
            inputStream = context.contentResolver.openInputStream(uri)
            imageView.setImageBitmap(BitmapFactory.decodeStream(inputStream, null, bounds))
        } catch (e: FileNotFoundException) {
            Toast.makeText(context, "Error opening image file", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Return `true` iff we are the profile owner of a managed profile.
     * Note that profile owner can be in primary user and secondary user too.
     */
    @TargetApi(VERSION_CODES.N)
    fun isManagedProfileOwner(context: Context): Boolean {
        val dpm = getDevicePolicyManager(context)
        return if (SDK_INT >= VERSION_CODES.N) {
            try {
                dpm.isManagedProfile(AdminReceiver.componentName)
            } catch (ex: SecurityException) {
                // This is thrown if we are neither profile owner nor device owner.
                false
            }
        } else isProfileOwner(context)

        // Pre-N, TestDPC only supports being the profile owner for a managed profile. Other apps
        // may support being a profile owner in other contexts (e.g. a secondary user) which will
        // require further checks.
    }

    @TargetApi(VERSION_CODES.M)
    fun isPrimaryUser(context: Context): Boolean {
        return if (SDK_INT >= VERSION_CODES.M) {
            val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
            userManager.isSystemUser
        } else {
            // Assume only DO can be primary user. This is not perfect but the cases in which it is
            // wrong are uncommon and require adb to set up.
            isDeviceOwner(context)
        }
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    fun isDeviceOwner(context: Context): Boolean {
        val dpm = getDevicePolicyManager(context)
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    fun isProfileOwner(context: Context): Boolean {
        val dpm = getDevicePolicyManager(context)
        return dpm.isProfileOwnerApp(context.packageName)
    }

    @TargetApi(VERSION_CODES.O)
    fun getBindDeviceAdminTargetUsers(context: Context): List<UserHandle> {
        if (SDK_INT < VERSION_CODES.O) {
            return emptyList()
        }
        val dpm = getDevicePolicyManager(context)
        return dpm.getBindDeviceAdminTargetUsers(AdminReceiver.componentName)
    }

    fun showFileViewer(fragment: PreferenceFragment, requestCode: Int) {
        val certIntent = Intent(Intent.ACTION_GET_CONTENT)
        certIntent.setTypeAndNormalize("*/*")
        try {
            fragment.startActivityForResult(certIntent, requestCode)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "showFileViewer: ", e)
        }
    }

    /**
     * @return If the certificate was successfully installed.
     */
    fun installCaCertificate(
        certificateInputStream: InputStream?, dpm: DevicePolicyManager, admin: ComponentName?
    ): Boolean {
        try {
            if (certificateInputStream != null) {
                val byteBuffer = ByteArrayOutputStream()
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var len = 0
                while (certificateInputStream.read(buffer).also { len = it } > 0) {
                    byteBuffer.write(buffer, 0, len)
                }
                return dpm.installCaCert(admin, byteBuffer.toByteArray())
            }
        } catch (e: IOException) {
            Log.e(TAG, "installCaCertificate: ", e)
        }
        return false
    }

    /**
     * Returns the persistent device owner state which has been set by the device owner as an app
     * restriction on GmsCore or null if there is no such restriction set.
     */
    @TargetApi(VERSION_CODES.O)
    fun getPersistentDoStateFromApplicationRestriction(
        dpm: DevicePolicyManager, admin: ComponentName?
    ): String? {
        val restrictions = dpm.getApplicationRestrictions(admin, GMSCORE_PACKAGE)
        return restrictions.getString(PERSISTENT_DEVICE_OWNER_STATE)
    }

    /**
     * Sets the persistent device owner state by setting a special app restriction on GmsCore and
     * notifies GmsCore about the change by sending a broadcast.
     *
     * @param state The device owner state to be preserved across factory resets. If null, the
     * persistent device owner state and the corresponding restiction are cleared.
     */
    @TargetApi(VERSION_CODES.O)
    fun setPersistentDoStateWithApplicationRestriction(
        context: Context, dpm: DevicePolicyManager, admin: ComponentName?, state: String?
    ) {
        val restrictions = dpm.getApplicationRestrictions(admin, GMSCORE_PACKAGE)
        if (state == null) {
            // Clear the restriction
            restrictions.remove(PERSISTENT_DEVICE_OWNER_STATE)
        } else {
            // Set the restriction
            restrictions.putString(PERSISTENT_DEVICE_OWNER_STATE, state)
        }
        dpm.setApplicationRestrictions(admin, GMSCORE_PACKAGE, restrictions)
        val broadcastIntent = Intent(BROADCAST_ACTION_FRP_CONFIG_CHANGED)
        broadcastIntent.setPackage(GMSCORE_PACKAGE)
        broadcastIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        context.sendBroadcast(broadcastIntent)
    }

    /** @return Intent for the default home activity
     */
    val homeIntent: Intent
        get() {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addCategory(Intent.CATEGORY_HOME)
            return intent
        }

    /** @return IntentFilter for the default home activity
     */
    val homeIntentFilter: IntentFilter
        get() {
            val filter = IntentFilter(Intent.ACTION_MAIN)
            filter.addCategory(Intent.CATEGORY_HOME)
            filter.addCategory(Intent.CATEGORY_DEFAULT)
            return filter
        }

    private fun getDevicePolicyManager(context: Context): DevicePolicyManager {
        return context.getSystemService(Service.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    fun hasDelegation(context: Context, delegation: String?): Boolean {
        return if (VERSION.SDK_INT < VERSION_CODES.O) {
            false
        } else {
            context.devicePolicyManager.getDelegatedScopes(null, context.packageName).contains(delegation)
        }
    }
}