package com.miraiprjkt.letmecook.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.miraiprjkt.letmecook.R;
import com.miraiprjkt.letmecook.adapter.FavoriteRecipeAdapter;
import com.miraiprjkt.letmecook.database.DatabaseHelper;
import com.miraiprjkt.letmecook.model.Meal;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private FavoriteRecipeAdapter adapter;
    private List<Meal> favoriteMeals;
    private DatabaseHelper dbHelper;

    private LinearLayout emptyFavoritesLayout;
    private MaterialButton backToHomeButton;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        // Inisialisasi semua view
        recyclerView = view.findViewById(R.id.recycler_view_favorites);
        emptyFavoritesLayout = view.findViewById(R.id.layout_favorites_empty);
        backToHomeButton = view.findViewById(R.id.button_back_to_home);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(getContext());

        // Gunakan ArrayList agar bisa diubah-ubah
        favoriteMeals = new ArrayList<>();

        adapter = new FavoriteRecipeAdapter(getContext(), favoriteMeals);
        recyclerView.setAdapter(adapter);

        // Atur listener untuk tombol kembali ke home
        backToHomeButton.setOnClickListener(v -> {
            // Gunakan NavController untuk kembali ke home
            NavHostFragment.findNavController(this).navigate(R.id.navigation_home);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Muat ulang data setiap kali fragment ini ditampilkan
        loadFavorites();
    }

    private void loadFavorites() {
        // Hapus data lama dan isi dengan data terbaru dari database
        favoriteMeals.clear();
        List<Meal> newFavorites = dbHelper.getAllFavorites();
        if (newFavorites != null) {
            favoriteMeals.addAll(newFavorites);
        }

        // Beri tahu adapter bahwa data telah berubah
        adapter.notifyDataSetChanged();

        // Periksa apakah daftar kosong atau tidak untuk menampilkan UI yang sesuai
        checkEmptyState();
    }

    private void checkEmptyState() {
        if (favoriteMeals.isEmpty()) {
            // Jika kosong, tampilkan pesan dan tombol
            recyclerView.setVisibility(View.GONE);
            emptyFavoritesLayout.setVisibility(View.VISIBLE);
        } else {
            // Jika ada isinya, tampilkan daftar resep
            recyclerView.setVisibility(View.VISIBLE);
            emptyFavoritesLayout.setVisibility(View.GONE);
        }
    }
}