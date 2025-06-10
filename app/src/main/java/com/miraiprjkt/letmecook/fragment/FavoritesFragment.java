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
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
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
import java.util.Objects;

public class FavoritesFragment extends Fragment {
    private RecyclerView recyclerView;
    private FavoriteRecipeAdapter adapter;
    private List<Meal> allFavoriteMeals;
    private List<Meal> displayedFavoriteMeals;
    private DatabaseHelper dbHelper;
    private LinearLayout emptyFavoritesLayout;
    private MaterialButton backToHomeButton;
    private ImageView emptyPlateIcon;
    private TextView emptyTitle, emptySubtitle;
    private SearchView searchView;


    public FavoritesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_favorites);
        emptyFavoritesLayout = view.findViewById(R.id.layout_favorites_empty);
        backToHomeButton = view.findViewById(R.id.button_back_to_home);
        emptyPlateIcon = view.findViewById(R.id.image_view_empty_plate);
        emptyTitle = view.findViewById(R.id.text_view_empty_title);
        emptySubtitle = view.findViewById(R.id.text_view_empty_subtitle);
        searchView = view.findViewById(R.id.search_view_favorites);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(getContext());
        allFavoriteMeals = new ArrayList<>();
        displayedFavoriteMeals = new ArrayList<>();
        adapter = new FavoriteRecipeAdapter(getContext(), displayedFavoriteMeals);
        recyclerView.setAdapter(adapter);

        backToHomeButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNavView = getActivity().findViewById(R.id.nav_view);
                if (bottomNavView != null) {
                    bottomNavView.setSelectedItemId(R.id.navigation_home);
                }
            }
        });

        setupSearchView();
        setupEmptyStateIconTint();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        allFavoriteMeals.clear();
        List<Meal> newFavorites = dbHelper.getAllFavorites();
        if (newFavorites != null) {
            allFavoriteMeals.addAll(newFavorites);
        }
        filterFavorites(searchView.getQuery().toString());
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterFavorites(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFavorites(newText);
                return true;
            }
        });

        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeButton.setOnClickListener(v -> searchView.setQuery("", false));
    }

    private void filterFavorites(String query) {
        displayedFavoriteMeals.clear();
        if (query == null || query.isEmpty()) {
            displayedFavoriteMeals.addAll(allFavoriteMeals);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Meal meal : allFavoriteMeals) {
                if (meal.getStrMeal().toLowerCase().contains(lowerCaseQuery)) {
                    displayedFavoriteMeals.add(meal);
                }
            }
        }
        adapter.notifyDataSetChanged();
        checkEmptyState();
    }

    private void checkEmptyState() {
        if (allFavoriteMeals.isEmpty()) {
            searchView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            emptyFavoritesLayout.setVisibility(View.VISIBLE);
            emptyTitle.setText("Your cookbook is looking a bit lonely!");
            emptySubtitle.setText("Let's go on a recipe hunt and find something delicious to save.");
            emptyPlateIcon.setImageResource(R.drawable.ic_empty_plate);
            backToHomeButton.setVisibility(View.VISIBLE);
        } else {
            searchView.setVisibility(View.VISIBLE);
            if (displayedFavoriteMeals.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyFavoritesLayout.setVisibility(View.VISIBLE);
                emptyTitle.setText("No recipes found");
                emptySubtitle.setText("Try a different keyword or clear the search.");
                emptyPlateIcon.setImageResource(R.drawable.ic_search_off);
                backToHomeButton.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyFavoritesLayout.setVisibility(View.GONE);
            }
        }
        setupEmptyStateIconTint();
    }

    private void setupEmptyStateIconTint() {
        if (getContext() == null || emptyPlateIcon == null || emptyPlateIcon.getDrawable() == null) return;

        boolean isSearchOffIcon = Objects.equals(emptyPlateIcon.getDrawable().getConstantState(),
                ContextCompat.getDrawable(getContext(), R.drawable.ic_search_off).getConstantState());

        int colorAttr;
        int alpha;
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

        if (isSearchOffIcon) {
            colorAttr = com.google.android.material.R.attr.colorOnSurface;
            alpha = 255;
        } else {
            colorAttr = isNightMode ? com.google.android.material.R.attr.colorOnSurfaceVariant : com.google.android.material.R.attr.colorOnSurface;
            alpha = isNightMode ? 204 : 153;
        }

        int colorToApply = getThemeColor(getContext(), colorAttr);
        emptyPlateIcon.setColorFilter(colorToApply, PorterDuff.Mode.SRC_IN);
        emptyPlateIcon.setImageAlpha(alpha);
    }

    private int getThemeColor(@NonNull Context context, @AttrRes int colorAttr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, typedValue, true);
        return ContextCompat.getColor(context, typedValue.resourceId);
    }
}