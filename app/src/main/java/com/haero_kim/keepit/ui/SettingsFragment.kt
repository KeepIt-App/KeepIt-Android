package com.haero_kim.keepit.ui

import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.haero_kim.keepit.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == "email") {
            val email = Intent(ACTION_SENDTO)
            email.data = Uri.parse("mailto:mac-pro@kakao.com")
            activity?.startActivity(email)
        }

        return super.onPreferenceTreeClick(preference)
    }
}