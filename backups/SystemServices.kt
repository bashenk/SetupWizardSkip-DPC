@file:Suppress("unused", "FunctionName")

package net.csgstore.setupskip

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.*
import android.app.admin.DevicePolicyManager
import android.app.job.JobScheduler
import android.app.usage.NetworkStatsManager
import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothManager
import android.companion.CompanionDeviceManager
import android.content.ClipboardManager
import android.content.Context
import android.content.RestrictionsManager
import android.content.pm.ShortcutManager
import android.hardware.ConsumerIrManager
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.hardware.display.DisplayManager
import android.hardware.fingerprint.FingerprintManager
import android.hardware.input.InputManager
import android.hardware.usb.UsbManager
import android.location.LocationManager
import android.media.AudioManager
import android.media.midi.MidiManager
import android.media.projection.MediaProjectionManager
import android.media.session.MediaSessionManager
import android.media.tv.TvInputManager
import android.net.ConnectivityManager
import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.p2p.WifiP2pManager
import android.nfc.NfcManager
import android.os.*
import android.os.health.SystemHealthManager
import android.os.storage.StorageManager
import android.print.PrintManager
import android.telecom.TelecomManager
import android.telephony.CarrierConfigManager
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.CaptioningManager
import android.view.inputmethod.InputMethodManager
import android.view.textclassifier.TextClassificationManager
import androidx.annotation.RequiresApi

/**
 * @author Brian Shenk
 */

/**
 * Helper method to retrieve the system service for the specified [Class]
 *
 * Example: applicationContext.getSystemService<[WindowManager]>()
 * @see [Context.getSystemService]
 */
inline fun <reified Class> Context.getSystemService(): Class? =
    this.applicationContext.getSystemService(getSystemServiceName(Class::class.java) as String) as Class

/** Returns the AccessibilityManager instance. **/
val Context.accessibilityManager: AccessibilityManager
    get() = applicationContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

/** Returns the AccountManager instance. **/
val Context.accountManager: AccountManager
    get() = applicationContext.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager


/** Returns the ActivityManager instance. **/
val Context.activityManager: ActivityManager
    get() = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager


/** Returns the AlarmManager instance. **/
val Context.alarmManager: AlarmManager
    get() = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager


/** Returns the AppOpsManager instance. **/
@Suppress("SpellCheckingInspection") val Context.appOpsManager: AppOpsManager
    @SuppressLint("WrongConstant") @RequiresApi(
        Build.VERSION_CODES.KITKAT) get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        applicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    } else {
        applicationContext.getSystemService("appops") as AppOpsManager
    }


/** Returns the AudioManager instance. **/
val Context.audioManager: AudioManager
    get() = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager


/** Returns the BatteryManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.batteryManager: BatteryManager
    get() = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager


/** Returns the BluetoothManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2) val Context.bluetoothManager: BluetoothManager
    get() = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager


/** Returns the CameraManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.cameraManager: CameraManager
    get() = applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager


/** Returns the CaptioningManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.KITKAT) val Context.captioningManager: CaptioningManager
    get() = applicationContext.getSystemService(Context.CAPTIONING_SERVICE) as CaptioningManager


/** Returns the CarrierConfigManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.M) val Context.carrierConfigManager: CarrierConfigManager
    get() = applicationContext.getSystemService(Context.CARRIER_CONFIG_SERVICE) as CarrierConfigManager


/** Returns the ClipboardManager instance. **/
val Context.clipboardManager: ClipboardManager
    get() = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager


/** Returns the CompanionDeviceManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.O) val Context.companionDeviceManager: CompanionDeviceManager
    get() = applicationContext.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager


/** Returns the ConnectivityManager instance. **/
val Context.connectivityManager: ConnectivityManager
    get() = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


/** Returns the ConsumerIrManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.KITKAT) val Context.consumerIrManager: ConsumerIrManager
    get() = applicationContext.getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager


/** Returns the DevicePolicyManager instance. **/
val Context.devicePolicyManager: DevicePolicyManager
    get() = applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager


/** Returns the DisplayManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1) val Context.displayManager: DisplayManager
    get() = applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager


/** Returns the DownloadManager instance. **/
val Context.downloadManager: DownloadManager
    get() = applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager


/** Returns the FingerprintManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.M) val Context.fingerprintManager: FingerprintManager
    get() = applicationContext.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager


/** Returns the HardwarePropertiesManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.N) val Context.hardwarePropertiesManager: HardwarePropertiesManager
    get() = applicationContext.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as HardwarePropertiesManager


/** Returns the InputManager instance. **/
val Context.inputManager: InputManager
    get() = applicationContext.getSystemService(Context.INPUT_SERVICE) as InputManager


/** Returns the InputMethodManager instance. **/
val Context.inputMethodManager: InputMethodManager
    get() = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


/** Returns the JobScheduler instance. **/
@get:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.jobScheduler: JobScheduler
    get() = applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler


/** Returns the KeyguardManager instance. **/
val Context.keyguardManager: KeyguardManager
    get() = applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager


/** Returns the LayoutInflater instance. **/
val Context.layoutInflater: LayoutInflater
    get() = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


/** Returns the LocationManager instance. **/
val Context.locationManager: LocationManager
    get() = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager


/** Returns the MediaProjectionManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.mediaProjectionManager: MediaProjectionManager
    get() = applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager


/** Returns the MediaSessionManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.mediaSessionManager: MediaSessionManager
    get() = applicationContext.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager


/** Returns the MidiManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.M) val Context.midiManager: MidiManager
    get() = applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager


/** Returns the NetworkStatsManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.M) val Context.networkStatsManager: NetworkStatsManager
    get() = applicationContext.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager


/** Returns the NfcManager instance. **/
val Context.nfcManager: NfcManager
    get() = applicationContext.getSystemService(Context.NFC_SERVICE) as NfcManager


/** Returns the NotificationManager instance. **/
val Context.notificationManager: NotificationManager
    get() = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


/** Returns the NsdManager instance. **/
val Context.nsdManager: NsdManager
    get() = applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager


/** Returns the PowerManager instance. **/
val Context.powerManager: PowerManager
    get() = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager


/** Returns the PrintManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.KITKAT) val Context.printManager: PrintManager
    get() = applicationContext.getSystemService(Context.PRINT_SERVICE) as PrintManager


/** Returns the RestrictionsManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.restrictionsManager: RestrictionsManager
    get() = applicationContext.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager


/** Returns the SearchManager instance. **/
val Context.searchManager: SearchManager
    get() = applicationContext.getSystemService(Context.SEARCH_SERVICE) as SearchManager


/** Returns the SensorManager instance. **/
val Context.sensorManager: SensorManager
    get() = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager


/** Returns the ShortcutManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.N_MR1) val Context.shortcutManager: ShortcutManager
    get() = applicationContext.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager


/** Returns the StorageManager instance. **/
val Context.storageManager: StorageManager
    get() = applicationContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager


/** Returns the StorageStatsManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.O) val Context.storageStatsManager: StorageStatsManager
    get() = applicationContext.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager


/** Returns the SystemHealthManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.N) val Context.systemHealthManager: SystemHealthManager
    get() = applicationContext.getSystemService(Context.SYSTEM_HEALTH_SERVICE) as SystemHealthManager


/** Returns the TelecomManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.telecomManager: TelecomManager
    get() = applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager


/** Returns the TelephonyManager instance. **/
val Context.telephonyManager: TelephonyManager
    get() = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager


/** Returns the TextClassificationManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.O) val Context.textClassificationManager: TextClassificationManager
    get() = applicationContext.getSystemService(Context.TEXT_CLASSIFICATION_SERVICE) as TextClassificationManager


/** Returns the TvInputManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.tvInputManager: TvInputManager
    get() = applicationContext.getSystemService(Context.TV_INPUT_SERVICE) as TvInputManager


/** Returns the UiModeManager instance. **/
val Context.uiModeManager: UiModeManager
    get() = applicationContext.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager


/** Returns the UsageStatsManager instance. **/
@get:Suppress("WrongConstant", "SpellCheckingInspection") @get:RequiresApi(Build.VERSION_CODES.LOLLIPOP)
val Context.usageStatsManager: UsageStatsManager
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    } else {
        applicationContext.getSystemService("usagestats") as UsageStatsManager
    }


/** Returns the UsbManager instance. **/
val Context.usbManager: UsbManager
    get() = applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager


/** Returns the UserManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1) val Context.userManager: UserManager
    get() = applicationContext.getSystemService(Context.USER_SERVICE) as UserManager


/** Returns the WallpaperManager instance. **/
val Context.wallpaperManager: WallpaperManager
    get() = applicationContext.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager


/** Returns the WifiAwareManager instance. **/
@get:RequiresApi(Build.VERSION_CODES.O) val Context.wifiAwareManager: WifiAwareManager
    get() = applicationContext.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager


/** Returns the WifiManager instance. **/
val Context.wifiManager: WifiManager
    get() = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager


/** Returns the WifiP2pManager instance. **/
val Context.wifiP2pManager: WifiP2pManager
    get() = applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager


/** Returns the WindowManager instance. **/
val Context.windowManager: WindowManager
    get() = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

