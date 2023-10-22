package com.echo.skygazer.ui.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.echo.skygazer.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        // Grabs the settings and sets the preferences to those values
        // Preferences are saved under skygazer_preferences.xml
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}