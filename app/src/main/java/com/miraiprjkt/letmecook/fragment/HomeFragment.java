// app/src/main/java/com/miraiprjkt/letmecook/HomeFragment.java
package com.miraiprjkt.letmecook.fragment;

import android.content.Context; // Import Context
import android.net.ConnectivityManager; // Import ConnectivityManager
import android.net.NetworkInfo; // Import NetworkInfo
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final long SEARCH_DELAY = 1500;
    private static final int NUMBER_OF_DISCOVER_RECIPES = 8;

    private RecyclerView recyclerViewRecipes;
    private RecipeAdapter recipeAdapter;
    private List<Meal> mealList;
    private ApiService apiService;
    private ChipGroup chipGroupCategories;
    private SearchView searchViewRecipes;
    private ShimmerFrameLayout shimmerViewContainer;
    private LinearLayout layoutNoResults;
    private ImageView imageViewNoResultsIcon;
    private TextView textViewNoResultsMessage;
    private MaterialButton buttonRetry;

    private Timer searchTimer;
    private String currentSelectedCategoryChipText = "Discover";
    private Random randomGenerator = new Random();

    private String lastFailedAction = "";
    private String lastQueryOrCategory = "";


    private String[] funnyNoResultsMessages = {
            "Hmm, resepnya lagi ngumpet nih! Coba kata kunci lain?",
            "Dapur kita lagi kosong melompong untuk pencarian ini. Yuk, cari yang lain!",
            "Chef Google bilang: 'Resep tidak ditemukan, tapi jangan menyerah!'"
    };

    private String[] funnyNetworkErrorMessages = {
            "Waduh, sinyalnya lagi jalan-jalan! Coba 'Ulangi' nanti.",
            "Internetnya lagi masak rendang, lama nih. Klik 'Ulangi' aja!",
            "Server resepnya lagi tidur siang. Bangunin pakai tombol 'Ulangi' yuk!"
    };


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
        shimmerViewContainer = view.findViewById(R.id.shimmer_view_container);
        layoutNoResults = view.findViewById(R.id.layout_no_results);
        imageViewNoResultsIcon = view.findViewById(R.id.image_no_results_icon);
        textViewNoResultsMessage = view.findViewById(R.id.text_view_no_results_message);
        buttonRetry = view.findViewById(R.id.button_retry);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupRecyclerView();
        setupSearchView();
        setupRetryButton();

        // Panggil loadInitialData yang akan mengecek koneksi sebelum memuat
        loadInitialData();

        return view;
    }

    private boolean isNetworkAvailable() {
        if (getContext() == null) return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void loadInitialData() {
        if (isNetworkAvailable()) {
            showLoading(true); // Tampilkan shimmer HANYA jika ada jaringan dan mulai memuat
            loadCategories(); // loadCategories akan memicu loadDiscoverRecipes setelah chip dibuat
        } else {
            showLoading(false); // Pastikan shimmer tidak berjalan jika tidak ada jaringan
            showNoResults(true, "network"); // Langsung tampilkan error jaringan
            lastFailedAction = "initial_load"; // Konteks untuk tombol retry
        }
    }


    private void setupRecyclerView() {
        mealList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(getContext(), mealList, meal -> {
            Toast.makeText(getContext(), "Clicked: " + meal.getStrMeal(), Toast.LENGTH_SHORT).show();
            Bundle bundle = new Bundle();
            bundle.putString(RecipeDetailFragment.ARG_MEAL_ID, meal.getIdMeal());
            bundle.putString("mealName", meal.getStrMeal());
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_homeFragment_to_recipeDetailFragment, bundle);
        });
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecipes.setAdapter(recipeAdapter);
    }

    private void setupRetryButton() {
        buttonRetry.setOnClickListener(v -> {
            if ("initial_load".equals(lastFailedAction) || "categories".equals(lastFailedAction)) {
                loadInitialData(); // Coba muat ulang data awal (kategori dan discover)
            } else if ("discover".equals(lastFailedAction)) {
                loadDiscoverRecipes();
            } else if ("category".equals(lastFailedAction) && lastQueryOrCategory != null) {
                loadRecipesByCategory(lastQueryOrCategory);
            } else if ("search".equals(lastFailedAction) && lastQueryOrCategory != null) {
                searchRecipesByName(lastQueryOrCategory);
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            shimmerViewContainer.startShimmer();
            shimmerViewContainer.setVisibility(View.VISIBLE);
            recyclerViewRecipes.setVisibility(View.GONE);
            layoutNoResults.setVisibility(View.GONE);
        } else {
            shimmerViewContainer.stopShimmer();
            shimmerViewContainer.setVisibility(View.GONE);
        }
    }

    private void showNoResults(boolean show, String messageContext) {
        if (show) {
            recyclerViewRecipes.setVisibility(View.GONE);
            layoutNoResults.setVisibility(View.VISIBLE);
            String message;
            int iconResId = R.drawable.ic_search_off; // Default icon

            if ("network".equals(messageContext)) {
                message = funnyNetworkErrorMessages[randomGenerator.nextInt(funnyNetworkErrorMessages.length)];
                iconResId = R.drawable.ic_network_error;
                buttonRetry.setVisibility(View.VISIBLE);
            } else { // "no_results"
                message = funnyNoResultsMessages[randomGenerator.nextInt(funnyNoResultsMessages.length)];
                iconResId = R.drawable.ic_search_off;
                buttonRetry.setVisibility(View.GONE);
            }
            textViewNoResultsMessage.setText(message);
            if (getContext() != null) {
                imageViewNoResultsIcon.setImageResource(iconResId);
            }

        } else {
            layoutNoResults.setVisibility(View.GONE);
            recyclerViewRecipes.setVisibility(View.VISIBLE); // Hanya tampilkan jika tidak ada error/no_results
            buttonRetry.setVisibility(View.GONE);
        }
    }


    private void loadCategories() {
        // Tidak perlu showLoading(true) di sini karena sudah ditangani oleh loadInitialData
        // atau oleh method pemanggil lainnya (seperti loadRecipesByCategory)
        apiService.getCategories().enqueue(new Callback<CategoryList>() {
            @Override
            public void onResponse(@NonNull Call<CategoryList> call, @NonNull Response<CategoryList> response) {
                // Jika loadCategories dipanggil sebagai bagian dari initial load,
                // dan initial load yang menampilkan shimmer, maka shimmer dihentikan setelah discover recipes dimuat.
                if (response.isSuccessful() && response.body() != null && response.body().getCategories() != null) {
                    setupChipGroup(response.body().getCategories());
                } else {
                    Log.e(TAG, "Failed to load categories: " + response.code());
                    // Jika kategori gagal dimuat saat startup, tampilkan error
                    if (chipGroupCategories.getChildCount() == 0) { // Belum ada chip (termasuk discover)
                        showLoading(false); // Hentikan shimmer jika masih berjalan
                        showNoResults(true, "network");
                        lastFailedAction = "categories"; // Untuk retry loadCategories (atau initial_load)
                    }
                    Toast.makeText(getContext(), "Gagal memuat kategori.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoryList> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading categories: " + t.getMessage());
                // Jika kategori gagal dimuat saat startup, tampilkan error
                if (chipGroupCategories.getChildCount() == 0) {
                    showLoading(false); // Hentikan shimmer
                    showNoResults(true, "network");
                    lastFailedAction = "categories";
                }
                Toast.makeText(getContext(), "Error memuat kategori: Cek koneksi internet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupChipGroup(List<Category> categories) {
        if (getContext() == null) return;
        chipGroupCategories.removeAllViews();
        chipGroupCategories.setSingleSelection(true);

        Chip discoverChip = new Chip(getContext());
        discoverChip.setText("Discover");
        discoverChip.setId(View.generateViewId());
        discoverChip.setCheckable(true);
        discoverChip.setOnClickListener(v -> {
            if (((Chip)v).isChecked()) { // Hanya proses jika chip menjadi checked
                currentSelectedCategoryChipText = "Discover";
                searchViewRecipes.setQuery("", false);
                searchViewRecipes.clearFocus();
                loadDiscoverRecipes();
            }
        });
        chipGroupCategories.addView(discoverChip);

        for (Category category : categories) {
            Chip chip = new Chip(getContext());
            chip.setText(category.getStrCategory());
            chip.setId(View.generateViewId());
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                if (((Chip)v).isChecked()) { // Hanya proses jika chip menjadi checked
                    currentSelectedCategoryChipText = category.getStrCategory();
                    searchViewRecipes.setQuery("", false);
                    searchViewRecipes.clearFocus();
                    loadRecipesByCategory(category.getStrCategory());
                }
            });
            chipGroupCategories.addView(chip);
        }

        // Default selection and load (jika berhasil memuat kategori)
        if (chipGroupCategories.getChildCount() > 0) {
            Chip firstChip = (Chip) chipGroupCategories.getChildAt(0); // Discover chip
            firstChip.setChecked(true);
            // loadDiscoverRecipes() akan dipanggil oleh listener onClick dari discoverChip
            // atau jika ini adalah bagian dari initial load yang sukses, loadDiscoverRecipes akan dipanggil setelah kategori.
            // Untuk memastikan discover dimuat setelah kategori:
            if ("Discover".equals(currentSelectedCategoryChipText) && (mealList == null || mealList.isEmpty())) {
                loadDiscoverRecipes(); // Panggil eksplisit jika discover default dan belum ada data
            }
        }
    }


    private void loadDiscoverRecipes() {
        if (!isNetworkAvailable()) {
            showLoading(false);
            showNoResults(true, "network");
            lastFailedAction = "discover";
            return;
        }
        showLoading(true);
        lastFailedAction = "discover";
        lastQueryOrCategory = "";

        final List<Meal> fetchedDiscoverMeals = new ArrayList<>();
        final int[] successfulCalls = {0};
        final int[] totalCallsMade = {0};

        if (mealList != null) mealList.clear(); else mealList = new ArrayList<>();
        if (recipeAdapter != null) recipeAdapter.notifyDataSetChanged();


        for (int i = 0; i < NUMBER_OF_DISCOVER_RECIPES; i++) {
            apiService.getRandomMeal().enqueue(new Callback<MealList>() {
                @Override
                public void onResponse(@NonNull Call<MealList> call, @NonNull Response<MealList> response) {
                    totalCallsMade[0]++;
                    if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null && !response.body().getMeals().isEmpty()) {
                        fetchedDiscoverMeals.add(response.body().getMeals().get(0));
                        successfulCalls[0]++;
                    }
                    checkDiscoverFetchCompletion(totalCallsMade[0], fetchedDiscoverMeals);
                }

                @Override
                public void onFailure(@NonNull Call<MealList> call, @NonNull Throwable t) {
                    totalCallsMade[0]++;
                    Log.e(TAG, "API Call Failed (Random Meal): " + t.getMessage());
                    checkDiscoverFetchCompletion(totalCallsMade[0], fetchedDiscoverMeals);
                }
            });
        }
    }

    private void checkDiscoverFetchCompletion(int callsMade, List<Meal> meals) {
        if (callsMade == NUMBER_OF_DISCOVER_RECIPES) {
            showLoading(false);
            if (!meals.isEmpty()) {
                mealList.clear();
                mealList.addAll(meals);
                recipeAdapter.updateData(mealList);
                showNoResults(false, "");
            } else {
                mealList.clear();
                recipeAdapter.updateData(mealList);
                showNoResults(true, "network");
            }
        }
    }


    private void loadRecipesByCategory(String categoryName) {
        if (!isNetworkAvailable()) {
            showLoading(false);
            showNoResults(true, "network");
            lastFailedAction = "category";
            lastQueryOrCategory = categoryName;
            return;
        }
        lastFailedAction = "category";
        lastQueryOrCategory = categoryName;
        fetchMeals(apiService.filterByCategory(categoryName), "Failed to load recipes for category: " + categoryName);
    }

    private void searchRecipesByName(String query) {
        if (!isNetworkAvailable() && !query.isEmpty()) { // Hanya tampilkan error jika query tidak kosong & tidak ada network
            showLoading(false);
            showNoResults(true, "network");
            lastFailedAction = "search";
            lastQueryOrCategory = query;
            return;
        }
        // Jika query kosong, biarkan performSearch yang menanganinya (kembali ke discover/kategori)
        if (query.isEmpty()) {
            performSearch(""); // Akan memicu logika kembali ke default
            return;
        }

        lastFailedAction = "search";
        lastQueryOrCategory = query;
        fetchMeals(apiService.searchMeals(query), "Failed to search recipes for: " + query);
    }

    private void fetchMeals(Call<MealList> call, String logErrorMessagePrefix) {
        showLoading(true);
        if (mealList != null) mealList.clear(); else mealList = new ArrayList<>();
        if (recipeAdapter != null) recipeAdapter.notifyDataSetChanged();

        call.enqueue(new Callback<MealList>() {
            @Override
            public void onResponse(@NonNull Call<MealList> call, @NonNull Response<MealList> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Meal> newMeals = response.body().getMeals();
                    if (newMeals != null && !newMeals.isEmpty()) {
                        mealList.clear();
                        mealList.addAll(newMeals);
                        recipeAdapter.updateData(mealList);
                        showNoResults(false, "");
                    } else {
                        mealList.clear();
                        recipeAdapter.updateData(mealList);
                        showNoResults(true, "no_results");
                        Log.d(TAG, logErrorMessagePrefix + ": No meals found or null response.");
                    }
                } else {
                    mealList.clear();
                    recipeAdapter.updateData(mealList);
                    showNoResults(true, "network"); // Network error atau server error
                    Log.e(TAG, logErrorMessagePrefix + ": " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MealList> call, @NonNull Throwable t) {
                showLoading(false);
                mealList.clear();
                recipeAdapter.updateData(mealList);
                showNoResults(true, "network");
                Log.e(TAG, logErrorMessagePrefix + " (Network Error): " + t.getMessage());
            }
        });
    }


    private void setupSearchView() {
        searchViewRecipes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (searchTimer != null) searchTimer.cancel();
                performSearch(query);
                searchViewRecipes.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchTimer != null) searchTimer.cancel();
                searchTimer = new Timer();
                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> performSearch(newText));
                        }
                    }
                }, SEARCH_DELAY);
                return true;
            }
        });

        ImageView closeButton = searchViewRecipes.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                searchViewRecipes.setQuery("", false);
                searchViewRecipes.clearFocus();
                performSearch(""); // Memanggil performSearch dengan query kosong untuk reset
            });
        }
    }

    private void performSearch(String query) {
        if (query == null) return;

        if (!query.isEmpty()) {
            chipGroupCategories.clearCheck();
            currentSelectedCategoryChipText = ""; // Reset agar tidak kembali ke kategori saat search
            searchRecipesByName(query);
        } else {
            // Query kosong, kembalikan ke chip yang terakhir dipilih atau "Discover"
            boolean chipRestored = false;
            for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupCategories.getChildAt(i);
                if (chip.getText().toString().equals(currentSelectedCategoryChipText)) {
                    if(!chip.isChecked()){
                        chip.setChecked(true); // Ini akan memicu onClick listener chip
                    } else {
                        // Jika sudah checked, panggil listener secara manual jika perlu refresh
                        // atau panggil method load-nya langsung
                        if ("Discover".equals(currentSelectedCategoryChipText)) loadDiscoverRecipes();
                        else loadRecipesByCategory(currentSelectedCategoryChipText);
                    }
                    chipRestored = true;
                    break;
                }
            }
            if (!chipRestored && chipGroupCategories.getChildCount() > 0) {
                // Default ke Discover jika tidak ada yang cocok atau currentSelectedCategoryChipText kosong
                Chip firstChip = (Chip)chipGroupCategories.getChildAt(0); // Discover chip
                if(!firstChip.isChecked()){
                    firstChip.setChecked(true);
                } else {
                    loadDiscoverRecipes(); // Panggil langsung jika sudah tercentang
                }
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (shimmerViewContainer != null && shimmerViewContainer.getVisibility() == View.VISIBLE && !shimmerViewContainer.isShimmerStarted()) {
            shimmerViewContainer.startShimmer();
        }
    }

    @Override
    public void onPause() {
        if (shimmerViewContainer != null && shimmerViewContainer.isShimmerStarted()) {
            shimmerViewContainer.stopShimmer();
        }
        super.onPause();
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