// app/src/main/java/com/miraiprjkt/letmecook/HomeFragment.java
package com.miraiprjkt.letmecook.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView; // Pastikan import ini benar
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.miraiprjkt.letmecook.R;
import com.miraiprjkt.letmecook.adapter.RecipeAdapter;
import com.miraiprjkt.letmecook.model.Category;
import com.miraiprjkt.letmecook.model.CategoryList;
import com.miraiprjkt.letmecook.model.Meal;
import com.miraiprjkt.letmecook.model.MealList;
import com.miraiprjkt.letmecook.network.ApiService;
import com.miraiprjkt.letmecook.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final long SEARCH_DELAY = 2000; // 2 detik

    private RecyclerView recyclerViewRecipes;
    private RecipeAdapter recipeAdapter;
    private List<Meal> mealList;
    private ApiService apiService;
    private ChipGroup chipGroupCategories;
    private SearchView searchViewRecipes;
    private ProgressBar progressBarHome;
    private TextView textViewNoResults;

    private Timer searchTimer;
    private String currentSelectedCategory = null;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewRecipes = view.findViewById(R.id.recycler_view_recipes);
        chipGroupCategories = view.findViewById(R.id.chip_group_categories);
        searchViewRecipes = view.findViewById(R.id.search_view_recipes);
        progressBarHome = view.findViewById(R.id.progress_bar_home);
        textViewNoResults = view.findViewById(R.id.text_view_no_results);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupRecyclerView();
        loadCategories();
        loadInitialRecipes(); // Muat resep awal
        setupSearchView();

        return view;
    }

    private void setupRecyclerView() {
        mealList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(getContext(), mealList, meal -> {
            // Handle klik item resep, misalnya buka DetailActivity/Fragment
            Toast.makeText(getContext(), "Clicked: " + meal.getStrMeal(), Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
            // intent.putExtra("MEAL_ID", meal.getIdMeal());
            // startActivity(intent);
        });
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecipes.setAdapter(recipeAdapter);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBarHome.setVisibility(View.VISIBLE);
            recyclerViewRecipes.setVisibility(View.GONE);
            textViewNoResults.setVisibility(View.GONE);
        } else {
            progressBarHome.setVisibility(View.GONE);
        }
    }

    private void showNoResults(boolean show) {
        if (show) {
            textViewNoResults.setVisibility(View.VISIBLE);
            recyclerViewRecipes.setVisibility(View.GONE);
        } else {
            textViewNoResults.setVisibility(View.GONE);
            recyclerViewRecipes.setVisibility(View.VISIBLE);
        }
    }


    private void loadCategories() {
        showLoading(true);
        apiService.getCategories().enqueue(new Callback<CategoryList>() {
            @Override
            public void onResponse(Call<CategoryList> call, Response<CategoryList> response) {
                // showLoading(false) akan dipanggil setelah resep dimuat
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body().getCategories();
                    if (categories != null) {
                        setupChipGroup(categories);
                    }
                } else {
                    Log.e(TAG, "Failed to load categories: " + response.message());
                    Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CategoryList> call, Throwable t) {
                // showLoading(false) akan dipanggil setelah resep dimuat
                Log.e(TAG, "Error loading categories: " + t.getMessage());
                Toast.makeText(getContext(), "Error loading categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupChipGroup(List<Category> categories) {
        chipGroupCategories.removeAllViews(); // Hapus chip lama jika ada

        // Tambahkan Chip "All" atau "Default"
        Chip allChip = new Chip(getContext());
        allChip.setText("All Recipes");
        allChip.setCheckable(true);
        allChip.setChecked(true); // Set default "All" checked
        allChip.setOnClickListener(v -> {
            currentSelectedCategory = null; // Reset kategori
            searchViewRecipes.setQuery("", false); // Reset search bar
            loadInitialRecipes(); // Muat resep awal (misalnya by first letter 'a')
            chipGroupCategories.clearCheck(); // Hapus centang dari chip lain
            allChip.setChecked(true); // Centang kembali chip "All"
        });
        chipGroupCategories.addView(allChip);


        for (Category category : categories) {
            Chip chip = new Chip(getContext());
            chip.setText(category.getStrCategory());
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                if (chip.isChecked()) {
                    currentSelectedCategory = category.getStrCategory();
                    loadRecipesByCategory(currentSelectedCategory);
                } else {
                    // Jika chip di-uncheck, dan tidak ada chip lain yang ter-check,
                    // maka kembali ke "All Recipes" atau kondisi default.
                    // Atau biarkan, tergantung behavior yang diinginkan.
                    // Untuk singleSelection=true, ini mungkin tidak terlalu relevan
                    // karena hanya satu yang bisa ter-check.
                    // Jika tidak ada yang ter-check setelah uncheck, muat resep awal.
                    if (chipGroupCategories.getCheckedChipId() == View.NO_ID) {
                        allChip.setChecked(true); // Otomatis memicu listener "All"
                    }
                }
            });
            chipGroupCategories.addView(chip);
        }
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID && !allChip.isChecked()) {
                // Jika tidak ada chip yang tercentang (misalnya setelah kategori yang dipilih gagal dimuat dan user menghapus centang)
                // Maka secara default kembali ke 'All Recipes'
                allChip.setChecked(true);
            }
            // Jika ada chip lain yang tercentang selain 'All', maka hilangkan centang 'All'
            else if (checkedId != allChip.getId() && allChip.isChecked()) {
                allChip.setChecked(false);
            }
        });
    }


    private void loadInitialRecipes() {
        fetchMeals(apiService.listMealsByFirstLetter("a"), "Failed to load initial recipes");
    }

    private void loadRecipesByCategory(String categoryName) {
        if (categoryName == null || categoryName.equalsIgnoreCase("All Recipes")) {
            loadInitialRecipes();
        } else {
            fetchMeals(apiService.filterByCategory(categoryName), "Failed to load recipes for category: " + categoryName);
        }
    }

    private void searchRecipesByName(String query) {
        fetchMeals(apiService.searchMeals(query), "Failed to search recipes for: " + query);
    }

    private void fetchMeals(Call<MealList> call, String errorMessagePrefix) {
        showLoading(true);
        call.enqueue(new Callback<MealList>() {
            @Override
            public void onResponse(Call<MealList> call, Response<MealList> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Meal> newMeals = response.body().getMeals();
                    if (newMeals != null && !newMeals.isEmpty()) {
                        mealList.clear();
                        mealList.addAll(newMeals);
                        recipeAdapter.updateData(mealList);
                        showNoResults(false);
                    } else {
                        mealList.clear();
                        recipeAdapter.updateData(mealList); // Kosongkan list
                        showNoResults(true);
                        Log.d(TAG, errorMessagePrefix + ": No meals found or null response.");
                    }
                } else {
                    mealList.clear();
                    recipeAdapter.updateData(mealList);
                    showNoResults(true);
                    Log.e(TAG, errorMessagePrefix + ": " + response.code() + " " + response.message());
                    // Toast.makeText(getContext(), errorMessagePrefix, Toast.LENGTH_SHORT).show(); // Mungkin terlalu banyak toast
                }
            }

            @Override
            public void onFailure(Call<MealList> call, Throwable t) {
                showLoading(false);
                mealList.clear();
                recipeAdapter.updateData(mealList);
                showNoResults(true);
                Log.e(TAG, errorMessagePrefix + " (Network Error): " + t.getMessage());
                Toast.makeText(getContext(), "Network Error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupSearchView() {
        searchViewRecipes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // User menekan tombol search / enter
                if (searchTimer != null) {
                    searchTimer.cancel();
                }
                if (!query.isEmpty()) {
                    // Hapus centang kategori jika ada
                    chipGroupCategories.clearCheck();
                    // Jika chip "All" masih tercentang (karena clearCheck tidak memicu listener),
                    // pastikan ia tidak tercentang sebelum search.
                    Chip allChip = (Chip) chipGroupCategories.getChildAt(0); // Asumsi "All" adalah chip pertama
                    if (allChip != null && allChip.isChecked()) {
                        allChip.setChecked(false);
                    }
                    currentSelectedCategory = null;
                    searchRecipesByName(query);
                } else {
                    // Jika query kosong, kembali ke tampilan default (misalnya, kategori "All")
                    Chip allChip = (Chip) chipGroupCategories.getChildAt(0);
                    if (allChip != null) {
                        allChip.setChecked(true); // Ini akan memicu listener "All" untuk memuat resep awal
                    } else {
                        loadInitialRecipes();
                    }
                }
                return true; // Menandakan event telah di-handle
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchTimer != null) {
                    searchTimer.cancel();
                }
                searchTimer = new Timer();
                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // Jalankan di UI thread
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (getActivity() == null || !isAdded()) {
                                return; // Pastikan fragment masih ter-attach
                            }
                            if (!newText.isEmpty()) {
                                // Hapus centang kategori jika ada
                                chipGroupCategories.clearCheck();
                                Chip allChip = (Chip) chipGroupCategories.getChildAt(0); // Asumsi "All" adalah chip pertama
                                if (allChip != null && allChip.isChecked()) {
                                    allChip.setChecked(false);
                                }
                                currentSelectedCategory = null;
                                searchRecipesByName(newText);
                            } else if (searchViewRecipes.getQuery().length() == 0 && !searchViewRecipes.hasFocus()){
                                // Jika teks kosong dan search view tidak fokus (misalnya user menghapus semua teks dan klik di luar)
                                // Kembali ke tampilan default atau kategori yang terakhir dipilih sebelum search
                                if(currentSelectedCategory != null) {
                                    // Cari dan centang kembali chip kategori yang terakhir dipilih
                                    for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                                        Chip chip = (Chip) chipGroupCategories.getChildAt(i);
                                        if (chip.getText().toString().equalsIgnoreCase(currentSelectedCategory)) {
                                            chip.setChecked(true);
                                            break;
                                        }
                                    }
                                } else {
                                    Chip allChip = (Chip) chipGroupCategories.getChildAt(0);
                                    if (allChip != null) {
                                        allChip.setChecked(true);
                                    } else {
                                        loadInitialRecipes();
                                    }
                                }
                            } else if (newText.isEmpty() && chipGroupCategories.getCheckedChipId() == View.NO_ID) {
                                // Jika teks dikosongkan dan tidak ada kategori yang dipilih, kembali ke "All"
                                Chip allChip = (Chip) chipGroupCategories.getChildAt(0);
                                if (allChip != null) {
                                    allChip.setChecked(true); // Ini akan memicu listener "All"
                                } else {
                                    loadInitialRecipes();
                                }
                            }
                        });
                    }
                }, SEARCH_DELAY);
                return true; // Menandakan event telah di-handle
            }
        });
        // Listener tambahan untuk tombol close (X) di SearchView
        ImageView closeButton = searchViewRecipes.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                searchViewRecipes.setQuery("", false); // Hapus teks query
                searchViewRecipes.clearFocus(); // Hapus fokus
                // Kembali ke "All Recipes" atau kategori yang terakhir dipilih (jika ada)
                if (currentSelectedCategory != null) {
                    for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                        Chip chip = (Chip) chipGroupCategories.getChildAt(i);
                        if (chip.getText().toString().equalsIgnoreCase(currentSelectedCategory)) {
                            chip.setChecked(true); // Ini akan memicu filter by category
                            return;
                        }
                    }
                }
                // Jika tidak ada kategori sebelumnya atau currentSelectedCategory null, default ke "All"
                Chip allChip = (Chip) chipGroupCategories.getChildAt(0); // Asumsi "All" adalah chip pertama
                if (allChip != null) {
                    allChip.setChecked(true); // Ini akan memicu listener "All"
                } else {
                    loadInitialRecipes();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchTimer != null) {
            searchTimer.cancel();
            searchTimer = null;
        }
    }
}