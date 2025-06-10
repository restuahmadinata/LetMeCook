package com.miraiprjkt.letmecook.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.miraiprjkt.letmecook.R;
import com.miraiprjkt.letmecook.RecipeDetailActivity;
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
    private LottieAnimationView lottieLoaderView;
    private LinearLayout layoutNoResults;
    private ImageView imageViewNoResultsIcon;
    private TextView textViewNoResultsMessage;
    private MaterialButton buttonRetry;
    private Timer searchTimer;
    private String currentSelectedCategoryChipText = "Discover";
    private Random randomGenerator = new Random();
    private String lastFailedAction = "";
    private String lastQueryOrCategory = "";
    private boolean isLoading = false;

    private final List<String> defaultCategoryNames = Arrays.asList(
            "Beef", "Chicken", "Dessert", "Lamb", "Miscellaneous",
            "Pasta", "Pork", "Seafood", "Side", "Starter",
            "Vegan", "Vegetarian", "Breakfast", "Goat"
    );

    private final String[] funnyNoResultsMessages = {
            "Hmm, the recipe’s hiding right now! Try another keyword?",
            "Our kitchen’s totally empty for this search. Let’s find something else!",
            "Chef Google says: 'Recipe not found, but don’t give up!'"
    };

    private final String[] funnyNetworkErrorMessages = {
            "Yikes, the signal's out on a stroll! Try 'Retry' later.",
            "The internet’s busy cooking rendang—this might take a while. Just click 'Retry'!",
            "The recipe server's taking a nap. Wake it up with the 'Retry' button!"
    };

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewRecipes = view.findViewById(R.id.recycler_view_recipes);
        chipGroupCategories = view.findViewById(R.id.chip_group_categories);
        searchViewRecipes = view.findViewById(R.id.search_view_recipes);
        lottieLoaderView = view.findViewById(R.id.lottie_loader_view);
        layoutNoResults = view.findViewById(R.id.layout_no_results);
        imageViewNoResultsIcon = view.findViewById(R.id.image_no_results_icon);
        textViewNoResultsMessage = view.findViewById(R.id.text_view_no_results_message);
        buttonRetry = view.findViewById(R.id.button_retry);

        setupLottieTheme();

        apiService = RetrofitClient.getClient().create(ApiService.class);

        setupRecyclerView();
        setupSearchView();
        setupRetryButton();
        loadInitialData();

        return view;
    }

    private void setupLottieTheme() {
        if (getContext() == null || lottieLoaderView == null) {
            return;
        }

        int nightModeFlags = getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            KeyPath keyPath = new KeyPath("**", "Stroke 1", "Color");
            int colorForDarkMode = ContextCompat.getColor(getContext(), R.color.md_theme_onSurface);
            LottieValueCallback<Integer> colorCallback = new LottieValueCallback<>(colorForDarkMode);
            lottieLoaderView.addValueCallback(keyPath, LottieProperty.STROKE_COLOR, colorCallback);
        }
    }

    private boolean isNetworkAvailable() {
        if (getContext() == null) return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void loadInitialData() {
        chipGroupCategories.setVisibility(View.VISIBLE);
        populateChipGroupWithDefaults();

        if (isNetworkAvailable()) {
            loadCategoriesFromApi();
        } else {
            showLoading(false);
            showNoResults(true, "network");
            lastFailedAction = "initial_load";
        }
    }

    private void setupRecyclerView() {
        mealList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(getContext(), mealList, meal -> {
            if (!isAdded() || getContext() == null) return;

            Intent intent = new Intent(getContext(), RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_MEAL_ID, meal.getIdMeal());
            intent.putExtra(RecipeDetailActivity.EXTRA_MEAL_NAME, meal.getStrMeal());
            startActivity(intent);
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

    private void showLoading(boolean shouldLoad) {
        if (lottieLoaderView == null) return;
        if (shouldLoad) {
            lottieLoaderView.setVisibility(View.VISIBLE);
            recyclerViewRecipes.setVisibility(View.GONE);
            layoutNoResults.setVisibility(View.GONE);
        } else {
            lottieLoaderView.setVisibility(View.GONE);
        }
    }

    private void showNoResults(boolean show, String messageContext) {
        if (show) {
            recyclerViewRecipes.setVisibility(View.GONE);
            layoutNoResults.setVisibility(View.VISIBLE);
            String message;
            int iconResId;

            if ("network".equals(messageContext)) {
                message = funnyNetworkErrorMessages[randomGenerator.nextInt(funnyNetworkErrorMessages.length)];
                iconResId = R.drawable.ic_network_error;
                buttonRetry.setVisibility(View.VISIBLE);
            } else {
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
            if (mealList != null && !mealList.isEmpty()) {
                recyclerViewRecipes.setVisibility(View.VISIBLE);
            } else {
                recyclerViewRecipes.setVisibility(View.GONE);
            }
            buttonRetry.setVisibility(View.GONE);
        }
    }

    private void populateChipGroupWithDefaults() {
        if (getContext() == null) return;
        chipGroupCategories.removeAllViews();
        chipGroupCategories.setSingleSelection(true);

        Chip discoverChip = createCategoryChip("Discover", true);
        chipGroupCategories.addView(discoverChip);

        for (String categoryName : defaultCategoryNames) {
            chipGroupCategories.addView(createCategoryChip(categoryName, false));
        }

        if (chipGroupCategories.getChildCount() > 0) {
            Chip firstChip = (Chip) chipGroupCategories.getChildAt(0);
            firstChip.setChecked(true);
            currentSelectedCategoryChipText = "Discover";
            loadDiscoverRecipes();
        }
    }

    private Chip createCategoryChip(String categoryName, boolean isDiscoverChip) {
        Chip chip = new Chip(getContext());
        chip.setText(categoryName);
        chip.setId(View.generateViewId());
        chip.setCheckable(true);
        chip.setOnClickListener(v -> {
            if (((Chip) v).isChecked()) {
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
                if (!isAdded() || response.body() == null) return;
                setupChipGroupWithApiData(response.body().getCategories());
            }

            @Override
            public void onFailure(@NonNull Call<CategoryList> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "Error loading categories from API: " + t.getMessage());
            }
        });
    }

    private void setupChipGroupWithApiData(List<Category> apiCategories) {
        if (getContext() == null || apiCategories == null || apiCategories.isEmpty()) return;
        chipGroupCategories.removeAllViews();
        chipGroupCategories.setSingleSelection(true);

        chipGroupCategories.addView(createCategoryChip("Discover", true));
        for (Category category : apiCategories) {
            chipGroupCategories.addView(createCategoryChip(category.getStrCategory(), false));
        }

        for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategories.getChildAt(i);
            if (chip.getText().toString().equals(currentSelectedCategoryChipText)) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private void runLayoutAnimation() {
        if (recyclerViewRecipes == null || !isAdded()) {
            return;
        }
        final Context context = recyclerViewRecipes.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

        recyclerViewRecipes.setLayoutAnimation(controller);
        if (recyclerViewRecipes.getAdapter() != null) {
            recyclerViewRecipes.getAdapter().notifyDataSetChanged();
        }
        recyclerViewRecipes.scheduleLayoutAnimation();
    }

    private void loadDiscoverRecipes() {
        if (isLoading) return;
        isLoading = true;

        if (!isNetworkAvailable()) {
            showLoading(false);
            showNoResults(true, "network");
            lastFailedAction = "discover";
            isLoading = false;
            return;
        }

        showLoading(true);
        lastFailedAction = "discover";
        lastQueryOrCategory = "";

        final List<Meal> fetchedDiscoverMeals = new ArrayList<>();
        final int[] totalCallsMade = {0};

        if (mealList != null) mealList.clear();
        else mealList = new ArrayList<>();
        if (recipeAdapter != null) recipeAdapter.notifyDataSetChanged();

        for (int i = 0; i < NUMBER_OF_DISCOVER_RECIPES; i++) {
            apiService.getRandomMeal().enqueue(new Callback<MealList>() {
                @Override
                public void onResponse(@NonNull Call<MealList> call, @NonNull Response<MealList> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null) {
                        fetchedDiscoverMeals.addAll(response.body().getMeals());
                    }
                    totalCallsMade[0]++;
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
            isLoading = false;
            showLoading(false);
            if (!meals.isEmpty()) {
                mealList.clear();
                mealList.addAll(meals);
                recipeAdapter.updateData(mealList);
                showNoResults(false, "");
                runLayoutAnimation();
            } else {
                mealList.clear();
                recipeAdapter.updateData(mealList);
                showNoResults(true, "network");
            }
        }
    }

    private void loadRecipesByCategory(String categoryName) {
        lastFailedAction = "category";
        lastQueryOrCategory = categoryName;
        fetchMeals(apiService.filterByCategory(categoryName));
    }

    private void searchRecipesByName(String query) {
        if (query.isEmpty()) {
            performSearch("");
            return;
        }
        lastFailedAction = "search";
        lastQueryOrCategory = query;
        fetchMeals(apiService.searchMeals(query));
    }

    private void fetchMeals(Call<MealList> call) {
        if (isLoading) return;
        isLoading = true;

        showLoading(true);
        if (mealList != null) mealList.clear();
        else mealList = new ArrayList<>();
        if (recipeAdapter != null) recipeAdapter.notifyDataSetChanged();

        call.enqueue(new Callback<MealList>() {
            @Override
            public void onResponse(@NonNull Call<MealList> call, @NonNull Response<MealList> response) {
                isLoading = false;
                if (!isAdded()) return;
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Meal> newMeals = response.body().getMeals();
                    if (newMeals != null && !newMeals.isEmpty()) {
                        mealList.clear();
                        mealList.addAll(newMeals);
                        recipeAdapter.updateData(mealList);
                        showNoResults(false, "");
                        runLayoutAnimation();
                    } else {
                        mealList.clear();
                        recipeAdapter.updateData(mealList);
                        showNoResults(true, "no_results");
                    }
                } else {
                    mealList.clear();
                    recipeAdapter.updateData(mealList);
                    showNoResults(true, "network");
                }
            }

            @Override
            public void onFailure(@NonNull Call<MealList> call, @NonNull Throwable t) {
                isLoading = false;
                if (!isAdded()) return;
                showLoading(false);
                mealList.clear();
                recipeAdapter.updateData(mealList);
                showNoResults(true, "network");
            }
        });
    }

    private void setupSearchView() {
        if (searchViewRecipes == null) return;
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
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> performSearch(newText));
                        }
                    }
                }, SEARCH_DELAY);
                return true;
            }
        });

        ImageView closeButton = searchViewRecipes.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeButton.setOnClickListener(v -> {
            searchViewRecipes.setQuery("", false);
            searchViewRecipes.clearFocus();
            performSearch("");
        });
    }

    private void performSearch(String query) {
        if (query == null) return;
        if (!query.isEmpty()) {
            chipGroupCategories.clearCheck();
            currentSelectedCategoryChipText = "";
            searchRecipesByName(query);
        } else {
            if (chipGroupCategories.getCheckedChipId() == View.NO_ID && chipGroupCategories.getChildCount() > 0) {
                Chip firstChip = (Chip) chipGroupCategories.getChildAt(0);
                firstChip.setChecked(true);
                currentSelectedCategoryChipText = firstChip.getText().toString();
                loadDiscoverRecipes();
            }
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