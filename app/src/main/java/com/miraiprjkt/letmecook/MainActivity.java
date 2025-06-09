// app/src/main/java/com/miraiprjkt/letmecook/MainActivity.java
package com.miraiprjkt.letmecook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.miraiprjkt.letmecook.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private MenuProvider currentMenuProvider;
    private MaterialToolbar toolbar; // Menggunakan MaterialToolbar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyThemeOnStartup();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi Toolbar
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar); // Penting! Menetapkan toolbar sebagai action bar

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            navView.setOnItemSelectedListener(item -> {
                if (item.getItemId() == navController.getCurrentDestination().getId()) {
                    return false; // Mengembalikan false agar tidak ada efek re-selection
                }

                NavOptions navOptions = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(navController.getCurrentDestination().getId(), true, false)
                        .setEnterAnim(R.anim.slide_in_up)
                        .setPopExitAnim(R.anim.slide_out_down)
                        .build();
                // ============== AKHIR PERUBAHAN ==============
                navController.navigate(item.getItemId(), null, navOptions);
                return true;
            });

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                // Set judul pada action bar
                if (getSupportActionBar() != null) {
                    if (destination.getLabel() != null) {
                        getSupportActionBar().setTitle(destination.getLabel());
                    } else {
                        getSupportActionBar().setTitle(getString(R.string.app_name));
                    }
                }

                // Hapus menu provider yang lama
                if (currentMenuProvider != null) {
                    removeMenuProvider(currentMenuProvider);
                }

                // Tambahkan menu provider baru jika di fragment chat
                if (destination.getId() == R.id.navigation_ai_chat) {
                    currentMenuProvider = new MenuProvider() {
                        @Override
                        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                            menuInflater.inflate(R.menu.chat_menu, menu);
                        }
                        @Override
                        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                            return false; // Biarkan fragment yang menangani
                        }
                    };
                    addMenuProvider(currentMenuProvider, this, Lifecycle.State.RESUMED);
                }

                // Mengatur visibilitas tombol kembali
                boolean isTopLevelDestination = destination.getId() == R.id.navigation_home ||
                        destination.getId() == R.id.navigation_favorites ||
                        destination.getId() == R.id.navigation_settings ||
                        destination.getId() == R.id.navigation_ai_chat;

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(!isTopLevelDestination);
                }
            });

        } else {
            Log.e("MainActivity", "NavHostFragment not found!");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0); // Hanya atur padding kiri & kanan
            toolbar.setPadding(0, systemBars.top, 0, 0); // Atur padding atas untuk toolbar
            return insets;
        });
    }

    // Menangani klik tombol kembali di action bar
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void applyThemeOnStartup() {
        SharedPreferences sharedPreferences = getSharedPreferences(SettingsFragment.PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(SettingsFragment.KEY_THEME, false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}