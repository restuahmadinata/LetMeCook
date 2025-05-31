// app/src/main/java/com/miraiprjkt/letmecook/SettingsFragment.java
package com.miraiprjkt.letmecook.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.miraiprjkt.letmecook.R;

import android.content.SharedPreferences;
import android.preference.PreferenceManager; // atau androidx.preference.PreferenceManager

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        SwitchMaterial switchTheme = view.findViewById(R.id.switch_theme);

        // Inisialisasi status switch berdasarkan mode saat ini
        // (Ini adalah contoh sederhana, idealnya simpan preferensi tema di SharedPreferences)
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            switchTheme.setChecked(true);
        } else {
            switchTheme.setChecked(false);
        }

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // Idealnya simpan pilihan ini di SharedPreferences agar persisten
            // getActivity().recreate(); // Untuk menerapkan tema secara langsung, tapi ini akan me-restart activity
        });

        return view;
    }
}