package com.haero_kim.keepit.util

import android.content.Context
import android.content.SharedPreferences

class MySharedPreferences(context: Context) {
    private val prefName = "prefs"
    private val prefsKey = "canceledItemLink"
    private val prefs: SharedPreferences =
            context.getSharedPreferences(prefName, 0)

    var latestCanceledLink: String?
        get() = prefs.getString(prefsKey, "")
        set(value) = prefs.edit().putString(prefsKey, value).apply()
}