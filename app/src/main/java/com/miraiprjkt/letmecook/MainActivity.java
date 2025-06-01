// app/src/main/java/com/miraiprjkt/letmecook/MainActivity.java
package com.miraiprjkt.letmecook;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // <-- Tambahkan import Toolbar
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration; // <-- Tambahkan import AppBarConfiguration
import androidx.navigation.ui.NavigationUI;     // <-- Tambahkan import NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView;
// import androidx.activity.EdgeToEdge; // Hapus jika tidak digunakan, atau sesuaikan

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration; // <-- Tambahkan variabel ini

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Aktifkan jika diinginkan, pastikan tema mendukung
        setContentView(R.layout.activity_main);

        // 1. Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // <-- Atur Toolbar sebagai ActionBar

        // 2. Setup BottomNavigationView
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // 3. Setup NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // 4. Setup AppBarConfiguration
            // Tentukan top-level destinations Anda (fragment yang ada di BottomNavigationView)
            // Tombol "up" (panah kembali) tidak akan muncul untuk destinasi ini.
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_favorites, R.id.navigation_settings)
                    // Jika Anda memiliki DrawerLayout, Anda bisa menambahkannya di sini:
                    // .setOpenableLayout(drawerLayout)
                    .build();

            // 5. Hubungkan NavController dengan ActionBar (Toolbar) dan BottomNavigationView
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navView, navController);

        } else {
            Log.e("MainActivity", "NavHostFragment not found!");
        }

        // Handle EdgeToEdge jika Anda tetap menggunakannya (sesuaikan ID jika perlu)
        // ID `main_container` adalah ID dari root ConstraintLayout di activity_main.xml
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Penyesuaian padding untuk EdgeToEdge:
            // Toolbar akan menangani padding atas.
            // BottomNavigationView akan berada di bawah, jadi padding bawah pada container utama mungkin 0.
            // Sesuaikan padding kiri dan kanan jika perlu.
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            // Jika BottomNavigationView Anda overlap, Anda mungkin perlu padding bawah pada RecyclerView/ScrollView di dalam fragment,
            // atau pada container fragment itu sendiri, bukan di root Activity layout.
            return insets;
        });
    }

    // 6. Override onSupportNavigateUp untuk menangani tombol "up" (panah kembali) di ActionBar
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            return NavigationUI.navigateUp(navController, appBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}