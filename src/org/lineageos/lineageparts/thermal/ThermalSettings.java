/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.lineageparts.thermal;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import lineageos.app.LineageContextConstants;
import lineageos.thermal.IThermalInterface;

import org.lineageos.lineageparts.R;
import org.lineageos.lineageparts.SettingsPreferenceFragment;

import java.util.Locale;

public class ThermalSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String CPU_LIMIT_PREF = "thermal_cpu_limit";
    private static final String BATTERY_LIMIT_PREF = "thermal_battery_limit";

    private ListPreference mCpuLimitPref;
    private ListPreference mBatteryLimitPref;
    private IThermalInterface mInterface;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addPreferencesFromResource(R.xml.thermal_settings);
        requireActivity().getActionBar().setTitle(R.string.thermal_title);

        IBinder binder = ServiceManager.getService(
                LineageContextConstants.LINEAGE_THERMAL_INTERFACE);
        mInterface = IThermalInterface.Stub.asInterface(binder);

        mCpuLimitPref = findPreference(CPU_LIMIT_PREF);
        mCpuLimitPref.setOnPreferenceChangeListener(this);
        mBatteryLimitPref = findPreference(BATTERY_LIMIT_PREF);
        mBatteryLimitPref.setOnPreferenceChangeListener(this);

        if (mInterface == null) {
            mCpuLimitPref.setEnabled(false);
            mBatteryLimitPref.setEnabled(false);
            return;
        }

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        if (mInterface == null) {
            return;
        }
        try {
            mCpuLimitPref.setValue(Integer.toString(mInterface.getCpuLimit()));
            mCpuLimitPref.setSummary(buildSummary(mCpuLimitPref,
                    mInterface.getCpuLimit(), mInterface.getCpuTemperature()));
            mBatteryLimitPref.setValue(Integer.toString(mInterface.getBatteryLimit()));
            mBatteryLimitPref.setSummary(buildSummary(mBatteryLimitPref,
                    mInterface.getBatteryLimit(), mInterface.getBatteryTemperature()));
        } catch (RemoteException e) {
        }
    }

    private CharSequence buildSummary(ListPreference preference, int limit, int temperature) {
        String summary = getString(R.string.thermal_summary,
                preference.getEntry(), format(temperature));
        if (limit > 0 && temperature >= limit) {
            summary += getString(R.string.thermal_active);
        }
        return summary;
    }

    private static String format(int millidegC) {
        return String.format(Locale.getDefault(), "%.1f\u00a0\u00b0C", millidegC / 1000.0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (mInterface == null) {
            return false;
        }

        int limit = Integer.parseInt((String) value);
        try {
            if (preference == mCpuLimitPref) {
                if (limit == 0) {
                    mInterface.clearCpuLimit();
                } else {
                    mInterface.setCpuLimit(limit);
                }
            } else if (preference == mBatteryLimitPref) {
                if (limit == 0) {
                    mInterface.clearBatteryLimit();
                } else {
                    mInterface.setBatteryLimit(limit);
                }
            }
        } catch (RemoteException e) {
            return false;
        }

        refresh();
        return true;
    }
}
