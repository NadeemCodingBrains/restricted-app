package com.sp.restricted

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private var tapCount = 0
    private val ADMIN_PIN = "1234"
    private val UNINSTALL_PIN = "9988"

    private lateinit var mainKioskUI: View
    private lateinit var llAppSelection: View
    private lateinit var rvAppList: RecyclerView
    private lateinit var rvLauncherList: RecyclerView
    private lateinit var appAdapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, KioskDeviceAdminReceiver::class.java)

        initViews()
        hideSystemUI()
        setupKioskMode()
        loadLauncherApps()
    }

    private fun initViews() {
        mainKioskUI = findViewById(R.id.mainKioskUI)
        llAppSelection = findViewById(R.id.llAppSelection)
        rvAppList = findViewById(R.id.rvAppList)
        rvLauncherList = findViewById(R.id.rvLauncherList)
        
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val btnSave = findViewById<Button>(R.id.btnSaveSelection)
        val btnBack = findViewById<Button>(R.id.btnExitAdmin)
        val btnUninstall = findViewById<Button>(R.id.btnUninstallSelf)

        rvAppList.layoutManager = LinearLayoutManager(this)
        rvLauncherList.layoutManager = GridLayoutManager(this, 4)

        // 3-Click logic for the Settings Button
        btnSettings.setOnClickListener {
            tapCount++
            if (tapCount >= 3) {
                tapCount = 0
                showPinDialog()
            } else {
                Toast.makeText(this, "Tap ${3 - tapCount} more times for settings", Toast.LENGTH_SHORT).show()
            }
        }

        btnSave.setOnClickListener { applyAllowedApps() }

        btnBack.setOnClickListener {
            llAppSelection.visibility = View.GONE
            mainKioskUI.visibility = View.VISIBLE
            loadLauncherApps()
        }

        btnUninstall.setOnClickListener { showUninstallDialog() }
    }

    private fun showPinDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pin, null)
        val pinInput = dialogView.findViewById<EditText>(R.id.etPin)

        AlertDialog.Builder(this)
            .setTitle("Admin Login")
            .setView(dialogView)
            .setPositiveButton("Enter") { _, _ ->
                val enteredPin = pinInput.text.toString().trim()
                if (enteredPin == ADMIN_PIN) {
                    mainKioskUI.visibility = View.GONE
                    llAppSelection.visibility = View.VISIBLE
                    loadAppList()
                } else {
                    Toast.makeText(this, "Wrong PIN", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUninstallDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pin, null)
        val pinInput = dialogView.findViewById<EditText>(R.id.etPin)
        pinInput.hint = "Enter Uninstall PIN (9988)"

        AlertDialog.Builder(this)
            .setTitle("DANGER: Uninstall App")
            .setMessage("This will REMOVE Device Owner and Uninstall the app.")
            .setView(dialogView)
            .setPositiveButton("UNINSTALL") { _, _ ->
                val enteredPin = pinInput.text.toString().trim()
                if (enteredPin == UNINSTALL_PIN) {
                    performUninstall()
                } else {
                    Toast.makeText(this, "Wrong PIN: $enteredPin", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performUninstall() {
        try {
            stopLockTask()
            devicePolicyManager.setLockTaskPackages(adminComponent, arrayOf())
            if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
                devicePolicyManager.clearDeviceOwnerApp(packageName)
            }
            devicePolicyManager.removeActiveAdmin(adminComponent)
            
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadLauncherApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val sharedPref = getSharedPreferences("KioskPrefs", Context.MODE_PRIVATE)
        val allowedPackages = sharedPref.getStringSet("allowed_apps", setOf()) ?: setOf()

        val launcherApps = mutableListOf<AppInfo>()
        for (info in resolveInfos) {
            val pkgName = info.activityInfo.packageName
            if (pkgName != packageName && allowedPackages.contains(pkgName)) {
                launcherApps.add(AppInfo(info.loadLabel(pm).toString(), pkgName, info.loadIcon(pm), true))
            }
        }
        rvLauncherList.adapter = LauncherAdapter(launcherApps) { pkg ->
            packageManager.getLaunchIntentForPackage(pkg)?.let { startActivity(it) }
        }
    }

    private fun loadAppList() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val sharedPref = getSharedPreferences("KioskPrefs", Context.MODE_PRIVATE)
        val currentAllowed = sharedPref.getStringSet("allowed_apps", setOf()) ?: setOf()

        val appList = mutableListOf<AppInfo>()
        for (info in resolveInfos) {
            val pkgName = info.activityInfo.packageName
            if (pkgName != packageName) {
                appList.add(AppInfo(info.loadLabel(pm).toString(), pkgName, info.loadIcon(pm), currentAllowed.contains(pkgName)))
            }
        }
        appAdapter = AppAdapter(appList.sortedBy { it.name })
        rvAppList.adapter = appAdapter
    }

    private fun applyAllowedApps() {
        val allowedList = appAdapter.getAllowedPackages().toMutableList()
        if (!allowedList.contains(packageName)) allowedList.add(packageName)

        getSharedPreferences("KioskPrefs", Context.MODE_PRIVATE).edit().putStringSet("allowed_apps", allowedList.toSet()).apply()

        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
            devicePolicyManager.setLockTaskPackages(adminComponent, allowedList.toTypedArray())
            startLockTask()
        }
        llAppSelection.visibility = View.GONE
        mainKioskUI.visibility = View.VISIBLE
        loadLauncherApps()
    }

    private fun setupKioskMode() {
        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
            val sharedPref = getSharedPreferences("KioskPrefs", Context.MODE_PRIVATE)
            val allowedList = (sharedPref.getStringSet("allowed_apps", setOf()) ?: setOf()).toMutableList()
            if (!allowedList.contains(packageName)) allowedList.add(packageName)
            devicePolicyManager.setLockTaskPackages(adminComponent, allowedList.toTypedArray())
            startLockTask()
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onResume() {
        super.onResume()
        if (devicePolicyManager.isDeviceOwnerApp(packageName)) startLockTask()
        hideSystemUI()
    }

    override fun onBackPressed() {}
}