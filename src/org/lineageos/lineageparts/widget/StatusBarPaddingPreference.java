/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.lineageparts.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;

import androidx.preference.PreferenceDataStore;
import androidx.preference.SeekBarPreference;

/**
 * Status bar padding slider (in dp) stored in Settings.System.
 * When unset, shows the device's SystemUI overlay value.
 */
public class StatusBarPaddingPreference extends SeekBarPreference {

    private static final String SYSTEMUI = "com.android.systemui";

    public StatusBarPaddingPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        final ContentResolver resolver = context.getContentResolver();
        setPreferenceDataStore(new PreferenceDataStore() {
            @Override
            public int getInt(String key, int defValue) {
                String value = Settings.System.getStringForUser(resolver, key,
                        UserHandle.USER_CURRENT);
                try {
                    return value != null ? Integer.parseInt(value) : deviceDefault(context, key);
                } catch (NumberFormatException e) {
                    return deviceDefault(context, key);
                }
            }

            @Override
            public void putInt(String key, int value) {
                Settings.System.putIntForUser(resolver, key, value, UserHandle.USER_CURRENT);
            }
        });
    }

    /** Clears the user value so SystemUI falls back to the device default. */
    public void reset() {
        Settings.System.putStringForUser(getContext().getContentResolver(), getKey(), null,
                UserHandle.USER_CURRENT);
        setValue(getPersistedInt(0));
    }

    private static int deviceDefault(Context context, String key) {
        try {
            Resources res = context.getPackageManager().getResourcesForApplication(SYSTEMUI);
            boolean rtl = res.getConfiguration().getLayoutDirection()
                    == android.view.View.LAYOUT_DIRECTION_RTL;
            String dimen = key.endsWith("_top") ? "status_bar_padding_top"
                    : key.endsWith("_left") != rtl ? "status_bar_padding_start"
                    : "status_bar_padding_end";
            return Math.round(res.getDimension(res.getIdentifier(dimen, "dimen", SYSTEMUI))
                    / res.getDisplayMetrics().density);
        } catch (Exception e) {
            return 0;
        }
    }
}
