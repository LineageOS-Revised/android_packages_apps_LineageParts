/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.lineageparts.screenshot;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.ListPreference;

import org.lineageos.lineageparts.R;
import org.lineageos.lineageparts.SettingsPreferenceFragment;

public class ScreenshotSettings extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.screenshot_settings);

        ContentResolver resolver = requireContext().getContentResolver();
        ListPreference clipboard = findPreference(Settings.System.SCREENSHOT_CLIPBOARD_ONLY);
        clipboard.setValue(String.valueOf(
                Settings.System.getInt(resolver, Settings.System.SCREENSHOT_CLIPBOARD_ONLY, 0)));
        clipboard.setOnPreferenceChangeListener((pref, value) -> Settings.System.putInt(resolver,
                Settings.System.SCREENSHOT_CLIPBOARD_ONLY, Integer.parseInt((String) value)));
    }
}
