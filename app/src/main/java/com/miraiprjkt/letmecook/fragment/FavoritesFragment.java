package com.miraiprjkt.letmecook.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miraiprjkt.letmecook.R;
import com.miraiprjkt.letmecook.adapter.FavoriteRecipeAdapter;
import com.miraiprjkt.letmecook.database.DatabaseHelper;
import com.miraiprjkt.letmecook.model.Meal;

import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private FavoriteRecipeAdapter adapter;
    private List<Meal> favoriteMeals;
    private DatabaseHelper dbHelper;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(getContext());
        favoriteMeals = dbHelper.getAllFavorites();

        adapter = new FavoriteRecipeAdapter(getContext(), favoriteMeals);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        favoriteMeals.clear();
        favoriteMeals.addAll(dbHelper.getAllFavorites());
        adapter.notifyDataSetChanged();
    }
}