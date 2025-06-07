package com.miraiprjkt.letmecook.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private ImageView emptyPlateIcon;


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
        emptyPlateIcon = view.findViewById(R.id.image_view_empty_plate);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(getContext());
        favoriteMeals = new ArrayList<>();
        adapter = new FavoriteRecipeAdapter(getContext(), favoriteMeals);
        recyclerView.setAdapter(adapter);

        backToHomeButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNavView = getActivity().findViewById(R.id.nav_view);
                if (bottomNavView != null) {
                    bottomNavView.setSelectedItemId(R.id.navigation_home);
                }
            }
        });

        // Atur warna ikon berdasarkan tema saat ini
        setupEmptyStateIconTint();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        favoriteMeals.clear();
        List<Meal> newFavorites = dbHelper.getAllFavorites();
        if (newFavorites != null) {
            favoriteMeals.addAll(newFavorites);
        }

        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
        checkEmptyState();
    }

    private void checkEmptyState() {
        if (favoriteMeals != null && favoriteMeals.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyFavoritesLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyFavoritesLayout.setVisibility(View.GONE);
        }
    }

    private void setupEmptyStateIconTint() {
        if (getContext() == null || emptyPlateIcon == null) return;

        // Cek apakah mode gelap sedang aktif
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

        // ==================== PERUBAHAN DI SINI ====================
        // Menggunakan referensi atribut langsung dari library Material untuk menghindari error kompilasi.
        int colorAttr = isNightMode ? com.google.android.material.R.attr.colorOnSurfaceVariant : com.google.android.material.R.attr.colorOnSurface;
        // ==================== AKHIR PERUBAHAN ====================

        // Dapatkan nilai warna dari atribut
        int colorToApply = getThemeColor(getContext(), colorAttr);

        // Terapkan warna sebagai filter pada ikon
        emptyPlateIcon.setColorFilter(colorToApply, PorterDuff.Mode.SRC_IN);
        // Atur alpha (transparansi) secara terpisah
        emptyPlateIcon.setImageAlpha(isNightMode ? 204 : 153); // 80% alpha for dark, 60% for light
    }

    private int getThemeColor(@NonNull Context context, @AttrRes int colorAttr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, typedValue, true);
        return ContextCompat.getColor(context, typedValue.resourceId);
    }
}
