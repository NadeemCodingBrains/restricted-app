package com.kiosk.app

object KioskConfig {
    /**
     * Set the package name of the app you want to launch in kiosk mode.
     * Examples:
     *   "com.android.chrome"       -> Google Chrome
     *   "com.google.android.youtube" -> YouTube
     *   "com.whatsapp"             -> WhatsApp
     */
    const val TARGET_PACKAGE_NAME = "com.android.chrome"

    /**
     * Admin PIN to exit kiosk mode (tap the header 7 times to trigger)
     * Change this before deploying!
     */
    const val ADMIN_PIN = "1234"

    /**
     * Display name shown on the kiosk screen
     */
    const val KIOSK_TITLE = "Welcome"

    /**
     * Subtitle shown below the title
     */
    const val KIOSK_SUBTITLE = "Tap the button below to get started"

    /**
     * Label on the launch button
     */
    const val LAUNCH_BUTTON_LABEL = "Launch App"
}
