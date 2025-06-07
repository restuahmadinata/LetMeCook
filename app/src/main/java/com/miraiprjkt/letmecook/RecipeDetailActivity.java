package com.miraiprjkt.letmecook;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.miraiprjkt.letmecook.database.DatabaseHelper;
import com.miraiprjkt.letmecook.model.Meal;
import com.miraiprjkt.letmecook.model.MealList;
import com.miraiprjkt.letmecook.network.ApiService;
import com.miraiprjkt.letmecook.network.RetrofitClient;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MEAL_ID = "extra_meal_id";
    public static final String EXTRA_MEAL_NAME = "extra_meal_name";
    private static final String TAG = "RecipeDetailActivity";

    private String mealId;
    private ApiService apiService;
    private Random randomGenerator = new Random();

    private ImageView imageMealDetailThumb;
    private TextView textMealDetailName, textMealDetailCategory, textMealDetailArea;
    private TextView textMealDetailIngredients, textMealDetailSource;
    private LinearLayout layoutInstructionsContainer;
    private TextView labelSource;
    private ChipGroup chipGroupDetailTags;
    private MaterialButton buttonYoutube;
    private ProgressBar progressBarDetail;
    private NestedScrollView nestedScrollViewContent;
    private LinearLayout layoutDetailError;
    private ImageView imageDetailErrorIcon;
    private TextView textViewDetailErrorMessage;
    private MaterialButton buttonDetailRetry;
    private FloatingActionButton fabFavorite;

    private DatabaseHelper dbHelper;
    private boolean isFavorite = false;
    private Meal currentMeal;

    private final String[] funnyNetworkErrorMessages = {
            "Duh, resepnya gak mau keluar tanpa internet! Coba 'Ulangi'.",
            "Sinyal ke dapur resep lagi putus, sambungin lagi yuk!",
            "Resep ini lagi malu-malu, butuh internet biar PD. Klik 'Ulangi'."
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_recipe_detail);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);

        mealId = getIntent().getStringExtra(EXTRA_MEAL_ID);
        String mealName = getIntent().getStringExtra(EXTRA_MEAL_NAME);
        getSupportActionBar().setTitle(mealName != null ? mealName : "Recipe Detail");

        apiService = RetrofitClient.getClient().create(ApiService.class);

        initializeViews();

        buttonDetailRetry.setOnClickListener(v -> {
            if (mealId != null && !mealId.isEmpty()) {
                loadRecipeDetails(mealId);
            }
        });

        if (mealId != null && !mealId.isEmpty()) {
            loadRecipeDetails(mealId);
        } else {
            showErrorState("Recipe ID not found.", false);
        }
    }

    private void initializeViews() {
        nestedScrollViewContent = findViewById(R.id.scroll_view_recipe_detail_content);
        imageMealDetailThumb = findViewById(R.id.image_meal_detail_thumb);
        textMealDetailName = findViewById(R.id.text_meal_detail_name);
        chipGroupDetailTags = findViewById(R.id.chip_group_detail_tags);
        textMealDetailCategory = findViewById(R.id.text_meal_detail_category);
        textMealDetailArea = findViewById(R.id.text_meal_detail_area);
        textMealDetailIngredients = findViewById(R.id.text_meal_detail_ingredients);
        layoutInstructionsContainer = findViewById(R.id.layout_meal_detail_instructions_container);
        buttonYoutube = findViewById(R.id.button_youtube);
        textMealDetailSource = findViewById(R.id.text_meal_detail_source);
        labelSource = findViewById(R.id.label_source);
        progressBarDetail = findViewById(R.id.progress_bar_detail);
        layoutDetailError = findViewById(R.id.layout_detail_error);
        imageDetailErrorIcon = findViewById(R.id.image_detail_error_icon);
        textViewDetailErrorMessage = findViewById(R.id.text_view_detail_error_message);
        buttonDetailRetry = findViewById(R.id.button_detail_retry);
        fabFavorite = findViewById(R.id.fab_favorite);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showLoadingState(boolean isLoading) {
        progressBarDetail.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        fabFavorite.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if (isLoading) {
            nestedScrollViewContent.setVisibility(View.GONE);
            layoutDetailError.setVisibility(View.GONE);
        }
    }

    private void showErrorState(String customMessage, boolean isNetworkError) {
        showLoadingState(false);
        nestedScrollViewContent.setVisibility(View.GONE);
        layoutDetailError.setVisibility(View.VISIBLE);
        fabFavorite.setVisibility(View.GONE);

        String message;
        int iconResId = R.drawable.ic_search_off;

        if (isNetworkError) {
            message = (customMessage != null) ? customMessage : funnyNetworkErrorMessages[randomGenerator.nextInt(funnyNetworkErrorMessages.length)];
            iconResId = R.drawable.ic_network_error;
            buttonDetailRetry.setVisibility(View.VISIBLE);
        } else {
            message = (customMessage != null) ? customMessage : "An error occurred.";
            buttonDetailRetry.setVisibility(View.GONE);
        }
        textViewDetailErrorMessage.setText(message);
        imageDetailErrorIcon.setImageResource(iconResId);
    }

    private void showContentState() {
        showLoadingState(false);
        nestedScrollViewContent.setVisibility(View.VISIBLE);
        layoutDetailError.setVisibility(View.GONE);
        fabFavorite.setVisibility(View.VISIBLE);
    }

    private void loadRecipeDetails(String id) {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Offline mode detected. Trying to load from database.");
            Meal offlineMeal = dbHelper.getFavoriteMealById(id);
            if (offlineMeal != null) {
                Log.d(TAG, "Meal found in database. Displaying offline data.");
                currentMeal = offlineMeal;
                isFavorite = true; // Must be a favorite if it's in the DB
                updateFabIcon();
                displayRecipeDetails(offlineMeal);
                showContentState();
            } else {
                Log.d(TAG, "Meal not in database. Showing network error.");
                showErrorState("You are offline and this recipe is not saved in your favorites.", true);
            }
            return; // Stop further execution
        }

        showLoadingState(true);
        apiService.getMealDetails(id).enqueue(new Callback<MealList>() {
            @Override
            public void onResponse(@NonNull Call<MealList> call, @NonNull Response<MealList> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null && !response.body().getMeals().isEmpty()) {
                    currentMeal = response.body().getMeals().get(0);
                    isFavorite = dbHelper.isFavorite(currentMeal.getIdMeal());
                    updateFabIcon();
                    displayRecipeDetails(currentMeal);
                    showContentState();
                } else {
                    showErrorState("Failed to load recipe details. Code: " + response.code(), true);
                    Log.e(TAG, "Failed to load details: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MealList> call, @NonNull Throwable t) {
                showErrorState("Network error. Please try again.", true);
                Log.e(TAG, "Error loading details: " + t.getMessage());
            }
        });
    }

    private void displayRecipeDetails(Meal meal) {
        if (meal == null) {
            showErrorState("Recipe data is invalid.", false);
            return;
        }

        Glide.with(this)
                .load(meal.getStrMealThumb())
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.placeholder_food)
                .into(imageMealDetailThumb);

        textMealDetailName.setText(meal.getStrMeal());
        textMealDetailCategory.setText("Category: " + (meal.getStrCategory() != null ? meal.getStrCategory() : "N/A"));
        textMealDetailArea.setText("Area: " + (meal.getStrArea() != null ? meal.getStrArea() : "N/A"));

        chipGroupDetailTags.removeAllViews();
        if (meal.getStrTags() != null && !meal.getStrTags().trim().isEmpty()) {
            String[] tags = meal.getStrTags().split(",");
            for (String tag : tags) {
                if (tag.trim().isEmpty()) continue;
                Chip chip = new Chip(this);
                chip.setText(tag.trim());
                chipGroupDetailTags.addView(chip);
            }
            chipGroupDetailTags.setVisibility(View.VISIBLE);
        } else {
            chipGroupDetailTags.setVisibility(View.GONE);
        }

        List<String> ingredientsWithMeasures = meal.getIngredientsWithMeasures();
        if (!ingredientsWithMeasures.isEmpty()) {
            StringBuilder ingredientsBuilder = new StringBuilder();
            for (String item : ingredientsWithMeasures) {
                ingredientsBuilder.append("• ").append(item).append("\n");
            }
            textMealDetailIngredients.setText(ingredientsBuilder.toString().trim());
        } else {
            textMealDetailIngredients.setText("No ingredients listed.");
        }

        layoutInstructionsContainer.removeAllViews();
        String instructionsString = meal.getStrInstructions();
        if (instructionsString != null && !instructionsString.trim().isEmpty()) {
            String processedInstructions = instructionsString.trim();
            if (processedInstructions.toUpperCase().startsWith("DIRECTIONS:")) {
                int firstNewLine = processedInstructions.indexOf("\n");
                processedInstructions = (firstNewLine != -1) ? processedInstructions.substring(firstNewLine + 1).trim() : "";
            }

            String[] lines = processedInstructions.split("\\r?\\n");
            int actualStepNumber = 1;
            LayoutInflater inflater = LayoutInflater.from(this);

            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) continue;

                Pattern headerPattern = Pattern.compile("^(STEP|PART)\\s*\\d+\\s*([-:]|\\s-\\s)?\\s*(.+)$", Pattern.CASE_INSENSITIVE);
                boolean isSectionHeader = headerPattern.matcher(trimmedLine).matches() ||
                        (trimmedLine.equals(trimmedLine.toUpperCase()) && trimmedLine.length() > 2 && trimmedLine.length() < 50 && (trimmedLine.endsWith(":") || !trimmedLine.contains(".")) && !trimmedLine.matches("^\\d+\\..*"));

                if (isSectionHeader) {
                    TextView sectionHeaderView = new TextView(this);
                    sectionHeaderView.setText("➡ " + trimmedLine);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    int marginTop = getResources().getDimensionPixelSize(R.dimen.instruction_section_header_margin_top);
                    int marginBottom = getResources().getDimensionPixelSize(R.dimen.instruction_section_header_margin_bottom);
                    params.setMargins(0, marginTop, 0, marginBottom);
                    sectionHeaderView.setLayoutParams(params);
                    sectionHeaderView.setTextAppearance(this, com.google.android.material.R.style.TextAppearance_Material3_TitleSmall);
                    layoutInstructionsContainer.addView(sectionHeaderView);
                } else {
                    String instructionText = trimmedLine.replaceFirst("^\\d+\\.\\s*", "").trim();
                    if (instructionText.isEmpty()) continue;
                    View stepView = inflater.inflate(R.layout.item_instruction_step, layoutInstructionsContainer, false);
                    ((TextView) stepView.findViewById(R.id.text_step_number)).setText(String.valueOf(actualStepNumber++));
                    ((TextView) stepView.findViewById(R.id.text_step_instruction)).setText(instructionText);
                    layoutInstructionsContainer.addView(stepView);
                }
            }

            if (layoutInstructionsContainer.getChildCount() == 0) addNoInstructionsTextView();
        } else {
            addNoInstructionsTextView();
        }

        if (meal.getStrYoutube() != null && !meal.getStrYoutube().trim().isEmpty()) {
            buttonYoutube.setVisibility(View.VISIBLE);
            buttonYoutube.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(meal.getStrYoutube()));
                startActivity(intent);
            });
        } else {
            buttonYoutube.setVisibility(View.GONE);
        }

        if (meal.getStrSource() != null && !meal.getStrSource().trim().isEmpty()) {
            textMealDetailSource.setText(meal.getStrSource());
            textMealDetailSource.setVisibility(View.VISIBLE);
            labelSource.setVisibility(View.VISIBLE);
        } else {
            textMealDetailSource.setVisibility(View.GONE);
            labelSource.setVisibility(View.GONE);
        }

        fabFavorite.setOnClickListener(v -> {
            if (currentMeal != null) {
                if (isFavorite) {
                    dbHelper.removeFavorite(currentMeal.getIdMeal());
                    Toast.makeText(RecipeDetailActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.addFavorite(currentMeal);
                    Toast.makeText(RecipeDetailActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                }
                isFavorite = !isFavorite;
                updateFabIcon();
            }
        });
    }

    private void addNoInstructionsTextView() {
        TextView noInstructionsView = new TextView(this);
        noInstructionsView.setText("No instructions available.");
        noInstructionsView.setTextAppearance(this, com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.instruction_section_header_margin_top), 0, 0);
        noInstructionsView.setLayoutParams(params);
        layoutInstructionsContainer.addView(noInstructionsView);
    }

    private void updateFabIcon() {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favorite);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}