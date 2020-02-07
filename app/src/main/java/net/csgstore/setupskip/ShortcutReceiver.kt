package net.csgstore.setupskip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class ShortcutReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.action) {
            Unlock.ACTION_ADD_SHORTCUT-> Unlock.shortcutCreated = true
            Reset.ACTION_ADD_SHORTCUT -> Reset.shortcutCreated = true
            Uninstall.ACTION_ADD_SHORTCUT -> Uninstall.shortcutCreated = true
        }
    }

    companion object {
        const val EXTRA_SHORTCUT: String = "Shortcut"
        const val ACTION_SHORTCUT_ADDED = Intent.ACTION_CREATE_SHORTCUT
    }
}