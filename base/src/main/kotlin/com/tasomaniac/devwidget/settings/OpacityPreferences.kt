package com.tasomaniac.devwidget.settings

import android.content.SharedPreferences
import android.content.res.Resources
import com.tasomaniac.devwidget.R
import javax.inject.Inject

class OpacityPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val resources: Resources
) {

    private val key = resources.getString(R.string.pref_key_opacity)

    val opacity: Opacity
        get() {
            val value = sharedPreferences.getString(key, null)
            return PreferenceEntries.fromValue(resources, value) ?: Opacity.VISIBLE_50
        }
}
