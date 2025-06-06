// app/src/main/java/com/miraiprjkt/letmecook/fragment/SettingsFragment.java
package com.miraiprjkt.letmecook.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.miraiprjkt.letmecook.R;

public class SettingsFragment extends Fragment {

    // Konstanta untuk SharedPreferences
    public static final String PREFS_NAME = "ThemePrefs";
    public static final String KEY_THEME = "ThemeMode"; // true untuk Dark, false untuk Light

    private SharedPreferences sharedPreferences;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Inisialisasi SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        SwitchMaterial switchTheme = view.findViewById(R.id.switch_theme);

        // Atur status switch berdasarkan preferensi yang tersimpan
        // Default ke mode terang (false) jika belum ada preferensi
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_THEME, false);
        switchTheme.setChecked(isDarkMode);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Simpan pilihan pengguna ke SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_THEME, isChecked);
            editor.apply();

            // Terapkan tema
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        return view;
    }
}