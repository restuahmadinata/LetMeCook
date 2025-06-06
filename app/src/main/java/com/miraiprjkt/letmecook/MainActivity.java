// app/src/main/java/com/miraiprjkt/letmecook/MainActivity.java
package com.miraiprjkt.letmecook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.miraiprjkt.letmecook.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private TextView customTitleTextView;
    private ImageButton customBackButton;
    private NavController navController;
    private ConstraintLayout mainContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Panggil method untuk menerapkan tema sebelum yang lainnya
        applyThemeOnStartup();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContainer = findViewById(R.id.main_container);
        customTitleTextView = findViewById(R.id.text_title_custom);
        customBackButton = findViewById(R.id.button_back_custom);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            NavigationUI.setupWithNavController(navView, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                // Set judul
                if (arguments != null && arguments.containsKey("mealName") && destination.getId() == R.id.recipeDetailFragment) {
                    customTitleTextView.setText(arguments.getString("mealName"));
                } else if (destination.getLabel() != null) {
                    customTitleTextView.setText(destination.getLabel());
                } else {
                    customTitleTextView.setText(getString(R.string.app_name)); //
                }

                // Atur visibilitas dan posisi tombol kembali & judul
                ConstraintLayout.LayoutParams titleParams = (ConstraintLayout.LayoutParams) customTitleTextView.getLayoutParams();

                if (destination.getId() == R.id.navigation_home ||
                        destination.getId() == R.id.navigation_favorites ||
                        destination.getId() == R.id.navigation_settings) {
                    customBackButton.setVisibility(View.GONE);
                    // Posisikan judul ke kiri penuh jika tombol back hilang
                    titleParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    titleParams.goneStartMargin = getResources().getDimensionPixelSize(R.dimen.custom_title_margin_start_no_back);
                    // Hapus constraint ke tombol back jika ada
                    titleParams.startToEnd = ConstraintLayout.LayoutParams.UNSET;
                } else {
                    customBackButton.setVisibility(View.VISIBLE);
                    // Posisikan judul ke kanan tombol back
                    titleParams.startToEnd = R.id.button_back_custom;
                    titleParams.resolveLayoutDirection(mainContainer.getLayoutDirection()); // Penting untuk RTL
                    // Hapus constraint ke parent start jika ada
                    titleParams.startToStart = ConstraintLayout.LayoutParams.UNSET;

                }
                customTitleTextView.setLayoutParams(titleParams);
            });

            customBackButton.setOnClickListener(v -> navController.navigateUp());

        } else {
            Log.e("MainActivity", "NavHostFragment not found!");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            mainContainer.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    /**
     * Membaca preferensi tema dari SharedPreferences dan menerapkannya.
     * Panggil method ini di awal onCreate() untuk menghindari "flash" perubahan tema.
     */
    private void applyThemeOnStartup() {
        SharedPreferences sharedPreferences = getSharedPreferences(SettingsFragment.PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(SettingsFragment.KEY_THEME, false); // Default ke terang
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}