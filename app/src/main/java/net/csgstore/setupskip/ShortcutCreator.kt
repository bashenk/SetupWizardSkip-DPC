package net.csgstore.setupskip

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import net.csgstore.setupskip.ShortcutReceiver.Companion.ACTION_SHORTCUT_ADDED


class ShortcutCreator : Activity() {

    @delegate:SuppressLint("ObsoleteSdkInt")
    private val runOnce by lazy {
        if (Build.VERSION.SDK_INT >= 17)
            Settings.Global.getInt(this.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        else {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(this.contentResolver, Settings.Secure.ADB_ENABLED, 0) == 1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Companion.runOnce = runOnce || !this.isThisDeviceOwner
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            AdminReceiver.enableSettingsPermissions(this)
//            AdminReceiver.configureAccessibility(this)
//            AdminReceiver.enableAccessibilityService(this)
        }
        //        addResetShortcut()
//        addUnlockShortcut()
//        requestShortcuts()
//        makeShortcuts(this)
        makeResetShortcut()
        finish()
    }

    private fun makeResetShortcut() {
        if (Reset.shortcutCreated) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            Toast.makeText(this, "Android version is too low to make shortcuts (requires 7.1 or higher)", Toast.LENGTH_SHORT).show()
            return
        }
        val shortcutManager = this.applicationContext.getSystemService<ShortcutManager>(ShortcutManager::class.java) as ShortcutManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !shortcutManager.isRequestPinShortcutSupported) {
            Toast.makeText(this, "PinShortcut is not supported (Home screen not supported, or Android version is below 8.0", Toast.LENGTH_SHORT).show()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            shortcutManager.requestPinShortcut(
                ShortcutInfo.Builder(this, "dynamic-reset")
                    .setShortLabel(this.getString(R.string.reset_short_label))
                    .setLongLabel(this.getString(R.string.reset_long_label))
                    .setIcon(Icon.createWithResource(this, R.drawable.reset_foreground))
                    .setActivity(ComponentName(this, Reset::class.java))
                    .setIntent(Intent(ACTION_SHORTCUT_ADDED).setComponent(ComponentName(this, Reset::class.java)))
                    .build(), PendingIntent.getBroadcast(this, Reset.REQ_CODE_SHORTCUT_ADDED_CALLBACK, Intent(Reset.ACTION_ADD_SHORTCUT).setPackage(PACKAGE_NAME).setComponent(
                        ComponentName(this, ShortcutReceiver::class.java)), 0).intentSender)
        }
    }
    private fun makeUninstallShortcut() {
        if (Uninstall.shortcutCreated) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            Toast.makeText(this, "Android version is too low to make shortcuts (requires 7.1 or higher)", Toast.LENGTH_SHORT).show()
            return
        }
        val shortcutManager = this.applicationContext.getSystemService<ShortcutManager>(ShortcutManager::class.java) as ShortcutManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !shortcutManager.isRequestPinShortcutSupported) {
            Toast.makeText(this, "PinShortcut is not supported (Home screen not supported, or Android version is below 8.0", Toast.LENGTH_SHORT).show()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            shortcutManager.requestPinShortcut(
                ShortcutInfo.Builder(this, "dynamic-uninstall")
                    .setShortLabel(this.getString(R.string.uninstall_short_label))
                    .setLongLabel(this.getString(R.string.uninstall_long_label))
                    .setIcon(Icon.createWithResource(this, R.drawable.unlock_foreground))
                    .setActivity(ComponentName(this, Unlock::class.java))
                    .setIntent(Intent(ACTION_SHORTCUT_ADDED).setComponent(ComponentName(this, Uninstall::class.java)))
                    .build(), PendingIntent.getBroadcast(this, Uninstall.REQ_CODE_SHORTCUT_ADDED_CALLBACK, Intent(Uninstall.ACTION_ADD_SHORTCUT).setPackage(PACKAGE_NAME).setComponent(
                    ComponentName(this, ShortcutReceiver::class.java)), 0).intentSender)
        }
    }

    private fun makeUnlockShortcut() {
        if (Unlock.shortcutCreated) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            Toast.makeText(this, "Android version is too low to make shortcuts (requires 7.1 or higher)", Toast.LENGTH_SHORT).show()
            return
        }
        val shortcutManager = this.applicationContext.getSystemService<ShortcutManager>(ShortcutManager::class.java) as ShortcutManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !shortcutManager.isRequestPinShortcutSupported) {
            Toast.makeText(this, "PinShortcut is not supported (Home screen not supported, or Android version is below 8.0", Toast.LENGTH_SHORT).show()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            shortcutManager.requestPinShortcut(
                ShortcutInfo.Builder(this, "dynamic-unlock")
                    .setShortLabel(this.getString(R.string.unlock_short_label))
                    .setLongLabel(this.getString(R.string.unlock_long_label))
                    .setIcon(Icon.createWithResource(this, R.drawable.unlock_foreground))
                    .setActivity(ComponentName(this, Unlock::class.java))
                    .setIntent(Intent(ACTION_SHORTCUT_ADDED).setComponent(ComponentName(this, Unlock::class.java)))
                    .build(), PendingIntent.getBroadcast(this, Unlock.REQ_CODE_SHORTCUT_ADDED_CALLBACK, Intent(Unlock.ACTION_ADD_SHORTCUT).setPackage(PACKAGE_NAME).setComponent(
                    ComponentName(this, ShortcutReceiver::class.java)), 0).intentSender)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun requestShortcuts(): Boolean {
        val shortcutManager =
            applicationContext.getSystemService(ShortcutManager::class.java) as ShortcutManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !shortcutManager.isRequestPinShortcutSupported) {
            Toast.makeText(this, "Launcher does not support short cut icon", Toast.LENGTH_SHORT)
                .show()
            finish()
            return false
        }
        val reset: ShortcutInfo = ShortcutInfo.Builder(applicationContext, "reset").setIntent(
            Intent(applicationContext, Reset::class.java)
                .setAction(Intent.ACTION_MAIN)
                .setPackage(PACKAGE_NAME)
        ) // !!! intent's action must be set on oreo
            .setShortLabel(resources.getString(R.string.reset_short_label))
            .setLongLabel(resources.getString(R.string.reset_long_label))
            .setIcon(Icon.createWithResource(applicationContext, R.mipmap.reset))
            .build()
        shortcutManager.requestPinShortcut(reset, null)
        val unlock: ShortcutInfo = ShortcutInfo.Builder(applicationContext, "unlock").setIntent(
            Intent(applicationContext, Reset::class.java)
                .setAction(Intent.ACTION_MAIN)
                .setPackage(PACKAGE_NAME)
        ) // !!! intent's action must be set on oreo
            .setShortLabel(resources.getString(R.string.reset_short_label))
            .setLongLabel(resources.getString(R.string.reset_long_label))
            .setIcon(Icon.createWithResource(applicationContext, R.mipmap.reset))
            .build()
        shortcutManager.requestPinShortcut(unlock, null)
        return true
    }

    @Suppress("DEPRECATION")
    private fun addResetShortcut() {
        val shortcutIntent = Intent(applicationContext, Reset::class.java)
        shortcutIntent.action = Intent.ACTION_MAIN
        val addIntent = Intent()
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.reset_short_label))
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
            Intent.ShortcutIconResource.fromContext(applicationContext, R.mipmap.reset)
        )
        addIntent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
        addIntent.putExtra("duplicate", false) //may it's already there so don't duplicate
        applicationContext.sendBroadcast(addIntent)
    }
    @Suppress("DEPRECATION")
    private fun addUnlockShortcut() {
        val shortcutIntent = Intent(applicationContext, Unlock::class.java)
        shortcutIntent.action = Intent.ACTION_MAIN
        val addIntent = Intent()
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.unlock_short_label))
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
            Intent.ShortcutIconResource.fromContext(applicationContext, R.mipmap.unlock)
        )
        addIntent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
        addIntent.putExtra("duplicate", false) //may it's already there so don't duplicate
        applicationContext.sendBroadcast(addIntent)
    }

}