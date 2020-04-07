package com.zebra.jamesswinton.zebrapdfprintv2;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragmentCompat {

    // Debugging
    private static final String TAG = "SettingsFragment";

    // Constants


    // Private Variables


    // Public Variables

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Add Pref XML
        addPreferencesFromResource(R.xml.settings_pref);
    }
}
