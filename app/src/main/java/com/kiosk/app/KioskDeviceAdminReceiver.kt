package com.kiosk.app

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class KioskDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Kiosk Admin: Enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Kiosk Admin: Disabled", Toast.LENGTH_SHORT).show()
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        Toast.makeText(context, "Kiosk lock task started", Toast.LENGTH_SHORT).show()
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        Toast.makeText(context, "Kiosk lock task stopped", Toast.LENGTH_SHORT).show()
    }
}
