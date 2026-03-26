package com.sp.restricted

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    var isAllowed: Boolean = false
)