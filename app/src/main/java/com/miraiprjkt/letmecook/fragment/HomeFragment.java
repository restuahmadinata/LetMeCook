// app/src/main/java/com/miraiprjkt/letmecook/fragment/HomeFragment.java
package com.miraiprjkt.letmecook.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.util.Arrays;
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
    private String currentSelectedCategoryChipText = "Discover"; // Default
    private Random randomGenerator = new Random();

    private String lastFailedAction = "";
    private String lastQueryOrCategory = "";

    // Daftar kategori default yang akan selalu ditampilkan
    private final List<String> defaultCategoryNames = Arrays.asList(
            "Beef", "Chicken", "Dessert", "Lamb", "Miscellaneous", "Pasta" , "Pork", "Seafood", "Side", "Starter", "Vegan", "Vegetarian", "Breakfast", "Goat"
    );

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
        chipGroupCategories.setVisibility(View.VISIBLE);
        // ***** MODIFIKASI: Selalu populate dengan default chips *****
        populateChipGroupWithDefaults();

        if (isNetworkAvailable()) {
            showLoading(true);
            loadCategoriesFromApi(); // Coba muat kategori aktual dari API
        } else {
            showLoading(false);
            showNoResults(true, "network");
            lastFailedAction = "initial_load";
            // "Discover" chip (dan default lainnya) sudah ada.
            // Jika mealList kosong, loadDiscoverRecipes() yang dipicu dari populateChipGroupWithDefaults
            // akan langsung gagal karena cek jaringan di dalamnya.
        }
    }

    private void setupRecyclerView() {
        mealList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(getContext(), mealList, meal -> {
            if (!isAdded() || getContext() == null) return;
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
                loadInitialData();
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
        // ... (method ini tetap sama seperti sebelumnya)
        if (show) {
            recyclerViewRecipes.setVisibility(View.GONE);
            layoutNoResults.setVisibility(View.VISIBLE);
            String message;
            int iconResId = R.drawable.ic_search_off;

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
            if (getContext() != null && imageViewNoResultsIcon != null) {
                imageViewNoResultsIcon.setImageResource(iconResId);
            }

        } else {
            layoutNoResults.setVisibility(View.GONE);
            if (mealList != null && !mealList.isEmpty()){
                recyclerViewRecipes.setVisibility(View.VISIBLE);
            } else {
                recyclerViewRecipes.setVisibility(View.GONE);
            }
            buttonRetry.setVisibility(View.GONE);
        }
    }

    // ***** MODIFIKASI: Nama method diubah dan fungsinya diperjelas *****
    private void populateChipGroupWithDefaults() {
        if (getContext() == null) return;
        chipGroupCategories.removeAllViews();
        chipGroupCategories.setSingleSelection(true);

        // 1. Tambahkan Chip "Discover"
        Chip discoverChip = createCategoryChip("Discover", true); // true untuk isDiscover
        chipGroupCategories.addView(discoverChip);

        // 2. Tambahkan Kategori Default dari list
        for (String categoryName : defaultCategoryNames) {
            chipGroupCategories.addView(createCategoryChip(categoryName, false));
        }

        // 3. Set "Discover" sebagai default terpilih dan muat datanya
        boolean discoverChecked = false;
        if (chipGroupCategories.getChildCount() > 0) {
            Chip firstChip = (Chip) chipGroupCategories.getChildAt(0); // "Discover" Chip
            if (firstChip != null && "Discover".equals(firstChip.getText().toString())) {
                firstChip.setChecked(true);
                currentSelectedCategoryChipText = "Discover"; // Pastikan state konsisten
                discoverChecked = true;
            }
        }
        // Jika Discover terpilih (atau default) dan belum ada data resep, muat resep discover
        if (discoverChecked && (mealList == null || mealList.isEmpty())) {
            if (isAdded()) {
                loadDiscoverRecipes();
            }
        }
    }

    // Helper method untuk membuat Chip
    private Chip createCategoryChip(String categoryName, boolean isDiscoverChip) {
        Chip chip = new Chip(getContext());
        chip.setText(categoryName);
        chip.setId(View.generateViewId());
        chip.setCheckable(true);
        chip.setOnClickListener(v -> {
            if (((Chip)v).isChecked()) {
                currentSelectedCategoryChipText = categoryName;
                if (searchViewRecipes != null) {
                    searchViewRecipes.setQuery("", false);
                    searchViewRecipes.clearFocus();
                }
                if (isDiscoverChip) {
                    loadDiscoverRecipes();
                } else {
                    loadRecipesByCategory(categoryName);
                }
            }
        });
        return chip;
    }


    private void loadCategoriesFromApi() {
        apiService.getCategories().enqueue(new Callback<CategoryList>() {
            @Override
            public void onResponse(@NonNull Call<CategoryList> call, @NonNull Response<CategoryList> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getCategories() != null && !response.body().getCategories().isEmpty()) {
                    // Berhasil memuat dari API, setup ChipGroup dengan data API
                    setupChipGroupWithApiData(response.body().getCategories());
                } else {
                    Log.e(TAG, "Failed to load categories from API or categories are empty: " + response.code());
                    // Kategori API gagal dimuat, chip default sudah ditampilkan.
                    // Jika "Discover" masih aktif dan belum ada data resep, coba muat.
                    if ("Discover".equals(currentSelectedCategoryChipText) && (mealList == null || mealList.isEmpty())) {
                        if (isNetworkAvailable()) {
                            loadDiscoverRecipes();
                        } else {
                            showLoading(false);
                            showNoResults(true, "network");
                        }
                    } else if (!isNetworkAvailable()) {
                        showLoading(false);
                        showNoResults(true, "network");
                    } else {
                        if (mealList == null || mealList.isEmpty()) showLoading(false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoryList> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Error loading categories from API: " + t.getMessage());
                // Kategori API gagal dimuat, chip default sudah ditampilkan.
                if ("Discover".equals(currentSelectedCategoryChipText) && (mealList == null || mealList.isEmpty())) {
                    if (isNetworkAvailable()) {
                        loadDiscoverRecipes();
                    } else {
                        showLoading(false);
                        showNoResults(true, "network");
                    }
                } else if (!isNetworkAvailable()){
                    showLoading(false);
                    showNoResults(true, "network");
                } else {
                    if (mealList == null || mealList.isEmpty()) showLoading(false);
                }
            }
        });
    }

    // ***** MODIFIKASI: Nama method diubah dan fungsinya diperjelas *****
    private void setupChipGroupWithApiData(List<Category> apiCategories) {
        if (getContext() == null) return;
        chipGroupCategories.removeAllViews(); // Hapus chip default yang mungkin ada
        chipGroupCategories.setSingleSelection(true);

        // 1. Selalu tambahkan Chip "Discover" di awal
        chipGroupCategories.addView(createCategoryChip("Discover", true));

        // 2. Tambahkan Kategori dari API
        for (Category category : apiCategories) {
            chipGroupCategories.addView(createCategoryChip(category.getStrCategory(), false));
        }

        // 3. Set chip yang sesuai sebagai checked (berdasarkan currentSelectedCategoryChipText atau default ke Discover)
        boolean chipWasSet = false;
        Chip chipToSelect = null;

        if (currentSelectedCategoryChipText != null && !currentSelectedCategoryChipText.isEmpty()){
            for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupCategories.getChildAt(i);
                if (chip.getText().toString().equals(currentSelectedCategoryChipText)) {
                    chipToSelect = chip;
                    chipWasSet = true;
                    break;
                }
            }
        }

        if (!chipWasSet && chipGroupCategories.getChildCount() > 0) {
            chipToSelect = (Chip) chipGroupCategories.getChildAt(0); // Default ke Discover jika tidak ada yang cocok
            currentSelectedCategoryChipText = "Discover"; // Update state
        }

        if (chipToSelect != null) {
            chipToSelect.setChecked(true);
            // Panggil method load yang sesuai secara manual
            if ("Discover".equals(currentSelectedCategoryChipText)) {
                if (mealList == null || mealList.isEmpty()) loadDiscoverRecipes();
            } else {
                // Hanya muat jika kategori berubah atau mealList kosong
                if (mealList == null || mealList.isEmpty() || !lastQueryOrCategory.equals(currentSelectedCategoryChipText)) {
                    loadRecipesByCategory(currentSelectedCategoryChipText);
                }
            }
        } else if (chipGroupCategories.getChildCount() == 0) {
            // Jika API kategori kosong dan terjadi error sehingga chip tidak ada, fallback
            populateChipGroupWithDefaults();
        }
    }

    // ... (loadDiscoverRecipes, checkDiscoverFetchCompletion, loadRecipesByCategory, searchRecipesByName, fetchMeals tetap sama)
    // Pastikan semua method load ini memanggil isNetworkAvailable() di awal.
    private void loadDiscoverRecipes() {
        if (!isNetworkAvailable()) {
            showLoading(false);
            showNoResults(true, "network");
            lastFailedAction = "discover";
            return;
        }
        // ... (sisa implementasi loadDiscoverRecipes)
        showLoading(true);
        lastFailedAction = "discover";
        lastQueryOrCategory = "";

        final List<Meal> fetchedDiscoverMeals = new ArrayList<>();
        final int[] totalCallsMade = {0};

        if (mealList != null) mealList.clear(); else mealList = new ArrayList<>();
        if (recipeAdapter != null) recipeAdapter.notifyDataSetChanged();

        for (int i = 0; i < NUMBER_OF_DISCOVER_RECIPES; i++) {
            apiService.getRandomMeal().enqueue(new Callback<MealList>() {
                @Override
                public void onResponse(@NonNull Call<MealList> call, @NonNull Response<MealList> response) {
                    if (!isAdded()) return;
                    totalCallsMade[0]++;
                    if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null && !response.body().getMeals().isEmpty()) {
                        fetchedDiscoverMeals.add(response.body().getMeals().get(0));
                    }
                    checkDiscoverFetchCompletion(totalCallsMade[0], fetchedDiscoverMeals);
                }
                @Override
                public void onFailure(@NonNull Call<MealList> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
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
        if (!isNetworkAvailable() && !query.isEmpty()) {
            showLoading(false);
            showNoResults(true, "network");
            lastFailedAction = "search";
            lastQueryOrCategory = query;
            return;
        }
        if (query.isEmpty()) {
            performSearch("");
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
                if (!isAdded()) return;
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
                    showNoResults(true, "network");
                    Log.e(TAG, logErrorMessagePrefix + ": " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MealList> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showLoading(false);
                mealList.clear();
                recipeAdapter.updateData(mealList);
                showNoResults(true, "network");
                Log.e(TAG, logErrorMessagePrefix + " (Network Error): " + t.getMessage());
            }
        });
    }


    // setupSearchView dan performSearch tetap sama seperti yang Anda berikan
    private void setupSearchView() {
        if (searchViewRecipes == null) {
            Log.e(TAG, "SearchView is null in setupSearchView");
            return;
        }
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
                        if (getActivity() != null && isAdded()) {
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
                performSearch("");
            });
        }
    }

    private void performSearch(String query) {
        if (query == null) return;

        if (!query.isEmpty()) {
            chipGroupCategories.clearCheck();
            currentSelectedCategoryChipText = "";
            searchRecipesByName(query);
        } else {
            boolean restored = false;
            if (currentSelectedCategoryChipText != null && !currentSelectedCategoryChipText.isEmpty()) {
                for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupCategories.getChildAt(i);
                    if (chip.getText().toString().equals(currentSelectedCategoryChipText)) {
                        chip.setChecked(true);
                        // Panggil manual karena setChecked tidak memicu onClick
                        if ("Discover".equals(currentSelectedCategoryChipText)) loadDiscoverRecipes();
                        else loadRecipesByCategory(currentSelectedCategoryChipText);
                        restored = true;
                        break;
                    }
                }
            }

            if (!restored && chipGroupCategories.getChildCount() > 0) {
                Chip discoverChip = (Chip) chipGroupCategories.getChildAt(0);
                if (discoverChip != null) {
                    discoverChip.setChecked(true);
                    currentSelectedCategoryChipText = "Discover";
                    loadDiscoverRecipes();
                }
            } else if (chipGroupCategories.getChildCount() == 0) {
                populateChipGroupWithDefaults();
            }
        }
    }

    // onResume, onPause, onDestroyView tetap sama
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