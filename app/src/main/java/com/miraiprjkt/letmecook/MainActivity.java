// app/src/main/java/com/miraiprjkt/letmecook/MainActivity.java
package com.miraiprjkt.letmecook;

import android.os.Bundle;
import android.util.Log; // Tambahkan import Log
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
// Hapus import androidx.navigation.Navigation; jika tidak digunakan lagi secara langsung
import androidx.navigation.fragment.NavHostFragment; // << Tambahkan import ini
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.activity.EdgeToEdge; // Pastikan import ini ada jika Anda menggunakan EdgeToEdge

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Jika Anda ingin menggunakan EdgeToEdge, pastikan dipanggil sebelum setContentView
        // EdgeToEdge.enable(this); // Aktifkan baris ini jika Anda ingin menggunakan EdgeToEdge seperti pada kode awal Anda

        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Cara yang lebih aman untuk mendapatkan NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(navView, navController);
        } else {
            // Ini seharusnya tidak terjadi jika layout Anda benar
            Log.e("MainActivity", "NavHostFragment not found!");
            // Anda bisa menambahkan penanganan error lebih lanjut di sini jika perlu
        }

        // Sesuaikan ID dengan root layout Anda di activity_main.xml
        // Jika root layout Anda adalah main_container (seperti saran saya sebelumnya)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Anda mungkin perlu menyesuaikan padding ini tergantung pada bagaimana EdgeToEdge diterapkan
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Padding bottom mungkin dihandle oleh NavView atau tidak diperlukan di sini
            return insets;
        });
        // Jika Anda menggunakan ID "main" seperti pada kode awal Anda, pastikan ID tersebut ada di activity_main.xml
        // dan sesuaikan juga di atas: findViewById(R.id.main)
    }
}