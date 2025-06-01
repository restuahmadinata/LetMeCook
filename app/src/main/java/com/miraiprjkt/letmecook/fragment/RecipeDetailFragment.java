// app/src/main/java/com/miraiprjkt/letmecook/RecipeDetailFragment.java
package com.miraiprjkt.letmecook.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.miraiprjkt.letmecook.R;
import com.miraiprjkt.letmecook.model.Meal;
import com.miraiprjkt.letmecook.model.MealList;
import com.miraiprjkt.letmecook.network.ApiService;
import com.miraiprjkt.letmecook.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailFragment extends Fragment {

    private static final String TAG = "RecipeDetailFragment";
    public static final String ARG_MEAL_ID = "meal_id";

    private String mealId;
    private ApiService apiService;

    private ImageView imageMealDetailThumb;
    private TextView textMealDetailName, textMealDetailCategory, textMealDetailArea;
    private TextView textMealDetailIngredients, textMealDetailInstructions, textMealDetailSource;
    private TextView labelSource; // Untuk menampilkan/menyembunyikan label Source
    private ChipGroup chipGroupDetailTags;
    private Button buttonYoutube;
    private ProgressBar progressBarDetail;
    private TextView textViewDetailError;
    private View contentContainer; // Root ConstraintLayout untuk menyembunyikan saat loading/error

    public RecipeDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mealId = getArguments().getString(ARG_MEAL_ID);
        }
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        imageMealDetailThumb = view.findViewById(R.id.image_meal_detail_thumb);
        textMealDetailName = view.findViewById(R.id.text_meal_detail_name);
        chipGroupDetailTags = view.findViewById(R.id.chip_group_detail_tags);
        textMealDetailCategory = view.findViewById(R.id.text_meal_detail_category);
        textMealDetailArea = view.findViewById(R.id.text_meal_detail_area);
        textMealDetailIngredients = view.findViewById(R.id.text_meal_detail_ingredients);
        textMealDetailInstructions = view.findViewById(R.id.text_meal_detail_instructions);
        buttonYoutube = view.findViewById(R.id.button_youtube);
        textMealDetailSource = view.findViewById(R.id.text_meal_detail_source);
        labelSource = view.findViewById(R.id.label_source);
        progressBarDetail = view.findViewById(R.id.progress_bar_detail);
        textViewDetailError = view.findViewById(R.id.text_view_detail_error);
        contentContainer = (View) view.findViewById(R.id.image_meal_detail_thumb).getParent();

        // Atur tombol back di Toolbar (jika Activity memiliki Toolbar)
        // Ini memerlukan setup dengan NavController di Activity
        // ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(""); // Atau nama resep

        if (mealId != null && !mealId.isEmpty()) {
            loadRecipeDetails(mealId);
        } else {
            showError("Recipe ID not found.");
        }

        return view;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBarDetail.setVisibility(View.VISIBLE);
            if (contentContainer instanceof ViewGroup) {
                ((ViewGroup)contentContainer).setVisibility(View.GONE);
            }
            textViewDetailError.setVisibility(View.GONE);
        } else {
            progressBarDetail.setVisibility(View.GONE);
            if (contentContainer instanceof ViewGroup) {
                ((ViewGroup)contentContainer).setVisibility(View.VISIBLE);
            }
        }
    }

    private void showError(String message) {
        showLoading(false);
        if (contentContainer instanceof ViewGroup) {
            ((ViewGroup)contentContainer).setVisibility(View.GONE);
        }
        textViewDetailError.setText(message);
        textViewDetailError.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }


    private void loadRecipeDetails(String id) {
        showLoading(true);
        apiService.getMealDetails(id).enqueue(new Callback<MealList>() {
            @Override
            public void onResponse(@NonNull Call<MealList> call, @NonNull Response<MealList> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null && !response.body().getMeals().isEmpty()) {
                    Meal meal = response.body().getMeals().get(0); // Harusnya hanya satu meal
                    displayRecipeDetails(meal);
                } else {
                    showError("Failed to load recipe details. Code: " + response.code());
                    Log.e(TAG, "Failed to load details: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MealList> call, @NonNull Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Error loading details: " + t.getMessage());
            }
        });
    }

    private void displayRecipeDetails(Meal meal) {
        if (meal == null || getContext() == null) {
            showError("Recipe data is invalid.");
            return;
        }
        if (contentContainer instanceof ViewGroup) {
            ((ViewGroup)contentContainer).setVisibility(View.VISIBLE); // Pastikan konten terlihat
        }
        textViewDetailError.setVisibility(View.GONE); // Sembunyikan pesan error jika ada


        if (getActivity() != null) {
            // Mengatur judul ActionBar/Toolbar
            // ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(meal.getStrMeal());
            // Atau jika menggunakan NavController untuk title:
            // NavController navController = NavHostFragment.findNavController(this);
            // navController.getCurrentDestination().setLabel(meal.getStrMeal());
        }


        Glide.with(getContext())
                .load(meal.getStrMealThumb())
                .placeholder(R.drawable.ic_launcher_background) // Ganti dengan placeholder
                .error(R.drawable.ic_launcher_foreground)       // Ganti dengan error image
                .into(imageMealDetailThumb);

        textMealDetailName.setText(meal.getStrMeal());
        textMealDetailCategory.setText("Category: " + (meal.getStrCategory() != null ? meal.getStrCategory() : "N/A"));
        textMealDetailArea.setText("Area: " + (meal.getStrArea() != null ? meal.getStrArea() : "N/A"));

        // Tampilkan Tags
        chipGroupDetailTags.removeAllViews();
        if (meal.getStrTags() != null && !meal.getStrTags().isEmpty()) {
            String[] tags = meal.getStrTags().split(",");
            for (String tag : tags) {
                Chip chip = new Chip(getContext());
                chip.setText(tag.trim());
                // chip.setChipBackgroundColorResource(R.color.your_chip_color); // Kustomisasi jika perlu
                chipGroupDetailTags.addView(chip);
            }
            chipGroupDetailTags.setVisibility(View.VISIBLE);
        } else {
            chipGroupDetailTags.setVisibility(View.GONE);
        }

        // Tampilkan Bahan dan Takaran
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


        textMealDetailInstructions.setText(meal.getStrInstructions() != null ? meal.getStrInstructions().replace("\n", "\n\n") : "No instructions available.");

        if (meal.getStrYoutube() != null && !meal.getStrYoutube().trim().isEmpty()) {
            buttonYoutube.setVisibility(View.VISIBLE);
            buttonYoutube.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(meal.getStrYoutube()));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Could not open YouTube link.", Toast.LENGTH_SHORT).show();
                }
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
    }
}