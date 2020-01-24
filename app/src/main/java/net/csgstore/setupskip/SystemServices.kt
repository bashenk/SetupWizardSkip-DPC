@file:Suppress("unused", "FunctionName")

package net.csgstore.setupskip

import android.accounts.AccountManager
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
val Context.accessibilityManager: AccessibilityManager by wLazy<Context, AccessibilityManager> {
    applicationContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
}

/** Returns the AccountManager instance. **/
val Context.accountManager: AccountManager by wLazy<Context, AccountManager> {
    applicationContext.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
}


/** Returns the ActivityManager instance. **/
val Context.activityManager: ActivityManager by wLazy<Context, ActivityManager> {
    applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
}


/** Returns the AlarmManager instance. **/
val Context.alarmManager: AlarmManager by wLazy<Context, AlarmManager> {
    applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}


/** Returns the AppOpsManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.KITKAT) val Context.appOpsManager: AppOpsManager by wLazy<Context, AppOpsManager> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        applicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    } else {
        @Suppress("SpellCheckingInspection") applicationContext.getSystemService("appops") as AppOpsManager
    }
}

/** Returns the AudioManager instance. **/
val Context.audioManager: AudioManager by wLazy<Context, AudioManager> {
    applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
}


/** Returns the BatteryManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.batteryManager: BatteryManager by wLazy<Context, BatteryManager> {
    applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
}


/** Returns the BluetoothManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
val Context.bluetoothManager: BluetoothManager by wLazy<Context, BluetoothManager> {
    applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
}


/** Returns the CameraManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.cameraManager: CameraManager by wLazy<Context, CameraManager> {
    applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
}


/** Returns the CaptioningManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.KITKAT)
val Context.captioningManager: CaptioningManager by wLazy<Context, CaptioningManager> {
    applicationContext.getSystemService(Context.CAPTIONING_SERVICE) as CaptioningManager
}


/** Returns the CarrierConfigManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.M)
val Context.carrierConfigManager: CarrierConfigManager by wLazy<Context, CarrierConfigManager> {
    applicationContext.getSystemService(Context.CARRIER_CONFIG_SERVICE) as CarrierConfigManager
}


/** Returns the ClipboardManager instance. **/
val Context.clipboardManager: ClipboardManager by wLazy<Context, ClipboardManager> {
    applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
}


/** Returns the CompanionDeviceManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.O)
val Context.companionDeviceManager: CompanionDeviceManager by wLazy<Context, CompanionDeviceManager> {
    applicationContext.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
}


/** Returns the ConnectivityManager instance. **/
val Context.connectivityManager: ConnectivityManager by wLazy<Context, ConnectivityManager> {
    applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}


/** Returns the ConsumerIrManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.KITKAT)
val Context.consumerIrManager: ConsumerIrManager by wLazy<Context, ConsumerIrManager> {
    applicationContext.getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager
}


/** Returns the DevicePolicyManager instance. **/
val Context.devicePolicyManager: DevicePolicyManager by wLazy<Context, DevicePolicyManager> {
    applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
}


/** Returns the DisplayManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
val Context.displayManager: DisplayManager by wLazy<Context, DisplayManager> {
    applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
}


/** Returns the DownloadManager instance. **/
val Context.downloadManager: DownloadManager by wLazy<Context, DownloadManager> {
    applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
}


/** Returns the FingerprintManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.M)
val Context.fingerprintManager: FingerprintManager by wLazy<Context, FingerprintManager> {
    applicationContext.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
}


/** Returns the HardwarePropertiesManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.N)
val Context.hardwarePropertiesManager: HardwarePropertiesManager by wLazy<Context, HardwarePropertiesManager> {
    applicationContext.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as HardwarePropertiesManager
}


/** Returns the InputManager instance. **/
val Context.inputManager: InputManager by wLazy<Context, InputManager> {
    applicationContext.getSystemService(Context.INPUT_SERVICE) as InputManager
}


/** Returns the InputMethodManager instance. **/
val Context.inputMethodManager: InputMethodManager by wLazy<Context, InputMethodManager> {
    applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
}


/** Returns the JobScheduler instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.jobScheduler: JobScheduler by wLazy<Context, JobScheduler> {
    applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
}


/** Returns the KeyguardManager instance. **/
val Context.keyguardManager: KeyguardManager by wLazy<Context, KeyguardManager> {
    applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
}


/** Returns the LayoutInflater instance. **/
val Context.layoutInflater: LayoutInflater by wLazy<Context, LayoutInflater> {
    applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
}


/** Returns the LocationManager instance. **/
val Context.locationManager: LocationManager by wLazy<Context, LocationManager> {
    applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
}


/** Returns the MediaProjectionManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.LOLLIPOP)
val Context.mediaProjectionManager: MediaProjectionManager by wLazy<Context, MediaProjectionManager> {
    applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
}


/** Returns the MediaSessionManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.LOLLIPOP)
val Context.mediaSessionManager: MediaSessionManager by wLazy<Context, MediaSessionManager> {
    applicationContext.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
}


/** Returns the MidiManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.M) val Context.midiManager: MidiManager by wLazy<Context, MidiManager> {
    applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager
}


/** Returns the NetworkStatsManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.M)
val Context.networkStatsManager: NetworkStatsManager by wLazy<Context, NetworkStatsManager> {
    applicationContext.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
}


/** Returns the NfcManager instance. **/
val Context.nfcManager: NfcManager by wLazy<Context, NfcManager> {
    applicationContext.getSystemService(Context.NFC_SERVICE) as NfcManager
}


/** Returns the NotificationManager instance. **/
val Context.notificationManager: NotificationManager by wLazy<Context, NotificationManager> {
    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}


/** Returns the NsdManager instance. **/
val Context.nsdManager: NsdManager by wLazy<Context, NsdManager> {
    applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager
}


/** Returns the PowerManager instance. **/
val Context.powerManager: PowerManager by wLazy<Context, PowerManager> {
    applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
}


/** Returns the PrintManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.KITKAT) val Context.printManager: PrintManager by wLazy<Context, PrintManager> {
    applicationContext.getSystemService(Context.PRINT_SERVICE) as PrintManager
}


/** Returns the RestrictionsManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.LOLLIPOP)
val Context.restrictionsManager: RestrictionsManager by wLazy<Context, RestrictionsManager> {
    applicationContext.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
}


/** Returns the SearchManager instance. **/
val Context.searchManager: SearchManager by wLazy<Context, SearchManager> {
    applicationContext.getSystemService(Context.SEARCH_SERVICE) as SearchManager
}


/** Returns the SensorManager instance. **/
val Context.sensorManager: SensorManager by wLazy<Context, SensorManager> {
    applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
}


/** Returns the ShortcutManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.N_MR1) val Context.shortcutManager: ShortcutManager by wLazy<Context, ShortcutManager> {
    applicationContext.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
}


/** Returns the StorageManager instance. **/
val Context.storageManager: StorageManager by wLazy<Context, StorageManager> {
    applicationContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager
}


/** Returns the StorageStatsManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.O)
val Context.storageStatsManager: StorageStatsManager by wLazy<Context, StorageStatsManager> {
    applicationContext.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
}


/** Returns the SystemHealthManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.N)
val Context.systemHealthManager: SystemHealthManager by wLazy<Context, SystemHealthManager> {
    applicationContext.getSystemService(Context.SYSTEM_HEALTH_SERVICE) as SystemHealthManager
}


/** Returns the TelecomManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.telecomManager: TelecomManager by wLazy<Context, TelecomManager> {
    applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
}


/** Returns the TelephonyManager instance. **/
val Context.telephonyManager: TelephonyManager by wLazy<Context, TelephonyManager> {
    applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
}


/** Returns the TextClassificationManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.O)
val Context.textClassificationManager: TextClassificationManager by wLazy<Context, TextClassificationManager> {
    applicationContext.getSystemService(Context.TEXT_CLASSIFICATION_SERVICE) as TextClassificationManager
}


/** Returns the TvInputManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.LOLLIPOP) val Context.tvInputManager: TvInputManager by wLazy<Context, TvInputManager> {
    applicationContext.getSystemService(Context.TV_INPUT_SERVICE) as TvInputManager
}


/** Returns the UiModeManager instance. **/
val Context.uiModeManager: UiModeManager by wLazy<Context, UiModeManager> {
    applicationContext.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
}


/** Returns the UsageStatsManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.LOLLIPOP)
val Context.usageStatsManager: UsageStatsManager by wLazy<Context, UsageStatsManager> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    } else {
        @Suppress("SpellCheckingInspection") applicationContext.getSystemService("usagestats") as UsageStatsManager
    }
}

/** Returns the UsbManager instance. **/
val Context.usbManager: UsbManager by wLazy<Context, UsbManager> {
    applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
}


/** Returns the UserManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1) val Context.userManager: UserManager by wLazy<Context, UserManager> {
    applicationContext.getSystemService(Context.USER_SERVICE) as UserManager
}


/** Returns the WallpaperManager instance. **/
val Context.wallpaperManager: WallpaperManager by wLazy<Context, WallpaperManager> {
    applicationContext.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
}


/** Returns the WifiAwareManager instance. **/
@delegate:RequiresApi(Build.VERSION_CODES.O) val Context.wifiAwareManager: WifiAwareManager by wLazy<Context, WifiAwareManager> {
    applicationContext.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
}


/** Returns the WifiManager instance. **/
val Context.wifiManager: WifiManager by wLazy<Context, WifiManager> {
    applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
}


/** Returns the WifiP2pManager instance. **/
val Context.wifiP2pManager: WifiP2pManager by wLazy<Context, WifiP2pManager> {
    applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
}


/** Returns the WindowManager instance. **/
val Context.windowManager: WindowManager by wLazy<Context, WindowManager> {
    applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
}
