package com.kiosk.app

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sp.restricted.R

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private var tapCount = 0
    private val ADMIN_PIN = "1234" // Change this PIN as needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, KioskDeviceAdminReceiver::class.java)

        setContentView(R.layout.activity_main)

        hideSystemUI()
        setupKioskMode()
        setupUI()
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    private fun setupKioskMode() {
        // Start lock task (pin app) if device owner
        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            startLockTask()
        }
    }

    private fun setupUI() {
        val settingsArea = findViewById<TextView>(R.id.tvHiddenSettings)


            launchTargetApp()
        

        // Hidden admin access: tap logo 7 times
        settingsArea.setOnClickListener {
            tapCount++
            if (tapCount >= 7) {
                tapCount = 0
                showPinDialog()
            }
        }
    }

    private fun launchTargetApp() {
        val targetPackage = KioskConfig.TARGET_PACKAGE_NAME
        val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage)

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        } else {
            Toast.makeText(
                this,
                "App not found: $targetPackage\nPlease install it first.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showPinDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pin, null)
        val pinInput = dialogView.findViewById<EditText>(R.id.etPin)

        AlertDialog.Builder(this, R.style.KioskDialog)
            .setTitle("Admin Access")
            .setView(dialogView)
            .setPositiveButton("Unlock") { _, _ ->
                val enteredPin = pinInput.text.toString()
                if (enteredPin == ADMIN_PIN) {
                    exitKioskMode()
                } else {
                    Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exitKioskMode() {
        stopLockTask()
        Toast.makeText(this, "Kiosk mode disabled", Toast.LENGTH_SHORT).show()
        // Show system UI again
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
        finish()
    }

    // Prevent back button from exiting kiosk
    override fun onBackPressed() {
        // Do nothing - back button disabled in kiosk mode
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }
}
