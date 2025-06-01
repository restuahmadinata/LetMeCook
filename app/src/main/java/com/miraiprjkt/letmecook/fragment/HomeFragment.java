// app/src/main/java/com/miraiprjkt/letmecook/HomeFragment.java
package com.miraiprjkt.letmecook.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
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
    private static final long SEARCH_DELAY = 1500; // 1.5 detik (bisa disesuaikan)
    private static final int NUMBER_OF_DISCOVER_RECIPES = 8; // Jumlah resep acak untuk Discover

    private RecyclerView recyclerViewRecipes;
    private RecipeAdapter recipeAdapter;
    private List<Meal> mealList;
    private ApiService apiService;
    private ChipGroup chipGroupCategories;
    private SearchView searchViewRecipes;
    // private ProgressBar progressBarHome; // Diganti Shimmer
    private ShimmerFrameLayout shimmerViewContainer;
    private LinearLayout layoutNoResults;
    private ImageView imageViewNoResultsIcon;
    private TextView textViewNoResultsMessage;
    private MaterialButton buttonRetry;

    private Timer searchTimer;
    private String currentSelectedCategoryChipText = "Discover"; // Default ke Discover
    private Random randomGenerator = new Random();

    // Untuk Retry Button
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
        loadCategories(); // Ini juga akan memanggil loadDiscoverRecipes jika chip Discover pertama kali dibuat
        setupSearchView();
        setupRetryButton();

        return view;
    }

    private void setupRecyclerView() {
        mealList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(getContext(), mealList, meal -> {
            // Navigasi ke RecipeDetailFragment
            Bundle bundle = new Bundle();
            bundle.putString(RecipeDetailFragment.ARG_MEAL_ID, meal.getIdMeal());
            bundle.putString("mealName", meal.getStrMeal()); // Kirim nama untuk judul sementara

            // Dapatkan NavController dari HomeFragment
            NavController navController = NavHostFragment.findNavController(HomeFragment.this);
            navController.navigate(R.id.action_homeFragment_to_recipeDetailFragment, bundle);
        });
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecipes.setAdapter(recipeAdapter);
        // ... (animasi layout jika ada)
    }

    private void setupRetryButton() {
        buttonRetry.setOnClickListener(v -> {
            if ("discover".equals(lastFailedAction)) {
                loadDiscoverRecipes();
            } else if ("category".equals(lastFailedAction) && lastQueryOrCategory != null) {
                loadRecipesByCategory(lastQueryOrCategory);
            } else if ("search".equals(lastFailedAction) && lastQueryOrCategory != null) {
                searchRecipesByName(lastQueryOrCategory);
            } else if ("categories".equals(lastFailedAction)) {
                loadCategories(); // Jika gagal memuat kategori
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
            // Visibilitas RecyclerView atau layoutNoResults akan diatur setelahnya
        }
    }

    private void showNoResults(boolean show, String messageContext) {
        if (show) {
            recyclerViewRecipes.setVisibility(View.GONE); // Sembunyikan RecyclerView
            layoutNoResults.setVisibility(View.VISIBLE);
            String message;
            int iconResId;

            if ("network".equals(messageContext)) {
                message = funnyNetworkErrorMessages[randomGenerator.nextInt(funnyNetworkErrorMessages.length)];
                iconResId = R.drawable.ic_network_error; // Ganti dengan ikon error jaringan Anda
                buttonRetry.setVisibility(View.VISIBLE);
            } else { // "no_results"
                message = funnyNoResultsMessages[randomGenerator.nextInt(funnyNoResultsMessages.length)];
                iconResId = R.drawable.ic_search_off; // Ganti dengan ikon no results Anda
                buttonRetry.setVisibility(View.GONE);
            }
            textViewNoResultsMessage.setText(message);
            if (getContext() != null) { // Cek null untuk context
                imageViewNoResultsIcon.setImageResource(iconResId);
            }

        } else {
            layoutNoResults.setVisibility(View.GONE);
            recyclerViewRecipes.setVisibility(View.VISIBLE); // Tampilkan RecyclerView jika ada data
        }
    }


    private void loadCategories() {
        // Tidak menampilkan shimmer untuk kategori, karena ini proses cepat di latar belakang
        // Shimmer utama untuk pemuatan resep.
        apiService.getCategories().enqueue(new Callback<CategoryList>() {
            @Override
            public void onResponse(@NonNull Call<CategoryList> call, @NonNull Response<CategoryList> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCategories() != null) {
                    setupChipGroup(response.body().getCategories());
                } else {
                    Log.e(TAG, "Failed to load categories: " + response.code());
                    Toast.makeText(getContext(), "Gagal memuat kategori.", Toast.LENGTH_SHORT).show();
                    lastFailedAction = "categories"; // Untuk retry
                    // Bisa tampilkan tombol retry global jika kategori gagal dimuat? Atau biarkan saja?
                    // Untuk saat ini, biarkan. Jika kategori gagal, "Discover" masih bisa berfungsi.
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoryList> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading categories: " + t.getMessage());
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                lastFailedAction = "categories";
            }
        });
    }

    private void setupChipGroup(List<Category> categories) {
        if (getContext() == null) { // Pastikan context tidak null
            return;
        }
        chipGroupCategories.removeAllViews();
        chipGroupCategories.setSingleSelection(true); // Tetap gunakan single selection

        // Chip "Discover" (menggunakan style default dari tema)
        Chip discoverChip = new Chip(getContext()); // Langsung buat instance Chip
        discoverChip.setText("Discover");
        discoverChip.setId(View.generateViewId()); // Penting untuk singleSelection dan state
        discoverChip.setCheckable(true); // Agar bisa di-check
        // discoverChip.setChecked(true); // Akan di-set setelah semua chip ditambahkan
        discoverChip.setOnClickListener(v -> {
            currentSelectedCategoryChipText = "Discover";
            searchViewRecipes.setQuery("", false);
            searchViewRecipes.clearFocus();
            loadDiscoverRecipes();
        });
        chipGroupCategories.addView(discoverChip);

        for (Category category : categories) {
            Chip chip = new Chip(getContext()); // Langsung buat instance Chip
            chip.setText(category.getStrCategory());
            chip.setId(View.generateViewId());
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                currentSelectedCategoryChipText = category.getStrCategory();
                searchViewRecipes.setQuery("", false);
                searchViewRecipes.clearFocus();
                loadRecipesByCategory(category.getStrCategory());
            });
            chipGroupCategories.addView(chip);
        }

        // Set "Discover" as checked by default and load its content
        // setelah semua chip ditambahkan dan ID-nya sudah ter-generate.
        if (chipGroupCategories.getChildCount() > 0) {
            Chip firstChip = (Chip) chipGroupCategories.getChildAt(0); // Ini adalah chip "Discover"
            // chipGroupCategories.check(firstChip.getId()); // Set chip pertama (Discover) terpilih
            // Atau jika Anda ingin memicu listener saat pertama kali:
            firstChip.setChecked(true); // Ini akan memicu listener dari chip Discover
            // jika OnCheckedChangeListener diatur di ChipGroup,
            // atau Anda bisa memanggil loadDiscoverRecipes() secara eksplisit di sini.

            // Untuk memastikan loadDiscoverRecipes dipanggil saat pertama kali jika belum ada listener global
            // yang menangani check awal:
            if (mealList.isEmpty()) { // Hanya muat jika daftar masih kosong
                loadDiscoverRecipes();
            }
        }
    }


    private void loadDiscoverRecipes() {
        showLoading(true);
        lastFailedAction = "discover";
        lastQueryOrCategory = ""; // Tidak ada query spesifik untuk discover

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
                    checkDiscoverFetchCompletion(totalCallsMade[0], successfulCalls[0], fetchedDiscoverMeals);
                }

                @Override
                public void onFailure(@NonNull Call<MealList> call, @NonNull Throwable t) {
                    totalCallsMade[0]++;
                    Log.e(TAG, "API Call Failed (Random Meal): " + t.getMessage());
                    checkDiscoverFetchCompletion(totalCallsMade[0], successfulCalls[0], fetchedDiscoverMeals);
                }
            });
        }
    }

    private void checkDiscoverFetchCompletion(int callsMade, int successful, List<Meal> meals) {
        if (callsMade == NUMBER_OF_DISCOVER_RECIPES) {
            showLoading(false);
            if (!meals.isEmpty()) {
                mealList.clear();
                mealList.addAll(meals);
                recipeAdapter.updateData(mealList); // Gunakan metode updateData
                showNoResults(false, "");
                // recyclerViewRecipes.scheduleLayoutAnimation(); // Jalankan animasi jika ada
            } else {
                mealList.clear();
                recipeAdapter.updateData(mealList);
                showNoResults(true, "network"); // Anggap gagal total sebagai error jaringan
            }
            Log.d(TAG, "Discover meals fetch complete. Success: " + successful + "/" + NUMBER_OF_DISCOVER_RECIPES);
        }
    }


    private void loadRecipesByCategory(String categoryName) {
        lastFailedAction = "category";
        lastQueryOrCategory = categoryName;
        fetchMeals(apiService.filterByCategory(categoryName), "Failed to load recipes for category: " + categoryName);
    }

    private void searchRecipesByName(String query) {
        lastFailedAction = "search";
        lastQueryOrCategory = query;
        fetchMeals(apiService.searchMeals(query), "Failed to search recipes for: " + query);
    }

    private void fetchMeals(Call<MealList> call, String logErrorMessagePrefix) {
        showLoading(true);
        if (mealList != null) mealList.clear(); else mealList = new ArrayList<>(); // Kosongkan list sebelum fetch baru
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
                        // recyclerViewRecipes.scheduleLayoutAnimation(); // Jalankan animasi jika ada
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
                searchViewRecipes.clearFocus(); // Sembunyikan keyboard
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
                // Kembali ke kategori yang terakhir dipilih atau "Discover"
                chipGroupCategories.clearCheck(); // Hapus semua centang dulu
                boolean chipRestored = false;
                for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupCategories.getChildAt(i);
                    if (chip.getText().toString().equals(currentSelectedCategoryChipText)) {
                        chip.setChecked(true); // Ini akan memicu OnClickListener chip tersebut
                        chipRestored = true;
                        break;
                    }
                }
                if (!chipRestored && chipGroupCategories.getChildCount() > 0) {
                    // Jika tidak ada yang cocok (misalnya currentSelectedCategoryChipText belum diset dengan benar), default ke Discover
                    ((Chip)chipGroupCategories.getChildAt(0)).setChecked(true);
                }
            });
        }
    }

    private void performSearch(String query) {
        if (query == null) return;

        if (!query.isEmpty()) {
            chipGroupCategories.clearCheck(); // Hapus centang kategori saat mencari
            currentSelectedCategoryChipText = ""; // Reset kategori terpilih sementara
            searchRecipesByName(query);
        } else {
            // Jika query kosong, kembali ke kategori terakhir yang aktif atau Discover
            chipGroupCategories.clearCheck();
            boolean chipRestored = false;
            for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupCategories.getChildAt(i);
                if (chip.getText().toString().equals(currentSelectedCategoryChipText) || (currentSelectedCategoryChipText.equals("Discover") && i == 0) ) {
                    if(!chip.isChecked()) chip.setChecked(true); else chip.callOnClick(); // Panggil click jika sudah tercentang untuk refresh
                    chipRestored = true;
                    break;
                }
            }
            if (!chipRestored && chipGroupCategories.getChildCount() > 0) {
                // Default ke Discover jika tidak ada yang cocok
                Chip firstChip = (Chip)chipGroupCategories.getChildAt(0);
                if(!firstChip.isChecked()) firstChip.setChecked(true); else firstChip.callOnClick();
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