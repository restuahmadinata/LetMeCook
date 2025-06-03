// app/src/main/java/com/miraiprjkt/letmecook/fragment/RecipeDetailFragment.java
package com.miraiprjkt.letmecook.fragment; // Path disesuaikan

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.miraiprjkt.letmecook.R; // Pastikan R diimport dengan benar
import com.miraiprjkt.letmecook.model.Meal;
import com.miraiprjkt.letmecook.model.MealList;
import com.miraiprjkt.letmecook.network.ApiService;
import com.miraiprjkt.letmecook.network.RetrofitClient;
import java.util.List;
import java.util.Random;
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
    private TextView textMealDetailIngredients, /*textMealDetailInstructions,*/ textMealDetailSource; // textMealDetailInstructions diganti LinearLayout
    private LinearLayout layoutInstructionsContainer; // Untuk menampung langkah instruksi
    private TextView labelSource;
    private ChipGroup chipGroupDetailTags;
    private MaterialButton buttonYoutube;
    private ProgressBar progressBarDetail;
    private NestedScrollView nestedScrollViewContent;

    private LinearLayout layoutDetailError;
    private ImageView imageDetailErrorIcon;
    private TextView textViewDetailErrorMessage;
    private MaterialButton buttonDetailRetry;
    private Random randomGenerator = new Random();

    private String[] funnyNetworkErrorMessages = {
            "Duh, resepnya gak mau keluar tanpa internet! Coba 'Ulangi'.",
            "Sinyal ke dapur resep lagi putus, sambungin lagi yuk!",
            "Resep ini lagi malu-malu, butuh internet biar PD. Klik 'Ulangi'."
    };

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

        nestedScrollViewContent = view.findViewById(R.id.scroll_view_recipe_detail_content);
        imageMealDetailThumb = view.findViewById(R.id.image_meal_detail_thumb);
        textMealDetailName = view.findViewById(R.id.text_meal_detail_name);
        chipGroupDetailTags = view.findViewById(R.id.chip_group_detail_tags);
        textMealDetailCategory = view.findViewById(R.id.text_meal_detail_category);
        textMealDetailArea = view.findViewById(R.id.text_meal_detail_area);
        textMealDetailIngredients = view.findViewById(R.id.text_meal_detail_ingredients);
        layoutInstructionsContainer = view.findViewById(R.id.layout_meal_detail_instructions_container); // Inisialisasi LinearLayout
        buttonYoutube = view.findViewById(R.id.button_youtube);
        textMealDetailSource = view.findViewById(R.id.text_meal_detail_source);
        labelSource = view.findViewById(R.id.label_source);
        progressBarDetail = view.findViewById(R.id.progress_bar_detail);

        layoutDetailError = view.findViewById(R.id.layout_detail_error);
        imageDetailErrorIcon = view.findViewById(R.id.image_detail_error_icon);
        textViewDetailErrorMessage = view.findViewById(R.id.text_view_detail_error_message);
        buttonDetailRetry = view.findViewById(R.id.button_detail_retry);

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

    private void showLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBarDetail.setVisibility(View.VISIBLE);
            if (nestedScrollViewContent != null) nestedScrollViewContent.setVisibility(View.GONE);
            layoutDetailError.setVisibility(View.GONE);
        } else {
            progressBarDetail.setVisibility(View.GONE);
        }
    }

    private void showErrorState(String customMessage, boolean isNetworkError) {
        showLoadingState(false);
        if (nestedScrollViewContent != null) nestedScrollViewContent.setVisibility(View.GONE);
        layoutDetailError.setVisibility(View.VISIBLE);

        String message;
        int iconResId = R.drawable.ic_search_off;

        if (isNetworkError) {
            message = funnyNetworkErrorMessages[randomGenerator.nextInt(funnyNetworkErrorMessages.length)];
            iconResId = R.drawable.ic_network_error;
            buttonDetailRetry.setVisibility(View.VISIBLE);
        } else {
            message = (customMessage != null) ? customMessage : "An error occurred.";
            buttonDetailRetry.setVisibility(View.GONE);
        }
        textViewDetailErrorMessage.setText(message);
        if (getContext() != null && imageDetailErrorIcon != null) {
            imageDetailErrorIcon.setImageResource(iconResId);
        }
    }

    private void showContentState() {
        showLoadingState(false);
        if (nestedScrollViewContent != null) nestedScrollViewContent.setVisibility(View.VISIBLE);
        layoutDetailError.setVisibility(View.GONE);
    }

    private void loadRecipeDetails(String id) {
        if (!isNetworkAvailable()) {
            showErrorState(null, true);
            return;
        }
        showLoadingState(true);
        apiService.getMealDetails(id).enqueue(new Callback<MealList>() {
            @Override
            public void onResponse(@NonNull Call<MealList> call, @NonNull Response<MealList> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null && !response.body().getMeals().isEmpty()) {
                    Meal meal = response.body().getMeals().get(0);
                    displayRecipeDetails(meal);
                    showContentState();
                } else {
                    showErrorState("Failed to load recipe details. Code: " + response.code(), true);
                    Log.e(TAG, "Failed to load details: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MealList> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showErrorState("Network error. Please try again.", true);
                Log.e(TAG, "Error loading details: " + t.getMessage());
            }
        });
    }

    private void displayRecipeDetails(Meal meal) {
        if (meal == null || getContext() == null) {
            showErrorState("Recipe data is invalid.", false);
            return;
        }
        layoutDetailError.setVisibility(View.GONE);
        if (nestedScrollViewContent != null) nestedScrollViewContent.setVisibility(View.VISIBLE);

        Glide.with(getContext())
                .load(meal.getStrMealThumb())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(imageMealDetailThumb);

        textMealDetailName.setText(meal.getStrMeal());
        textMealDetailCategory.setText("Category: " + (meal.getStrCategory() != null ? meal.getStrCategory() : "N/A"));
        textMealDetailArea.setText("Area: " + (meal.getStrArea() != null ? meal.getStrArea() : "N/A"));

        chipGroupDetailTags.removeAllViews();
        if (meal.getStrTags() != null && !meal.getStrTags().trim().isEmpty()) {
            String[] tags = meal.getStrTags().split(",");
            for (String tag : tags) {
                if (tag.trim().isEmpty()) continue;
                Chip chip = new Chip(getContext());
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

        // --- PERUBAHAN UNTUK INSTRUKSI ---
        layoutInstructionsContainer.removeAllViews(); // Bersihkan instruksi lama jika ada
        String instructionsString = meal.getStrInstructions();
        if (instructionsString != null && !instructionsString.trim().isEmpty()) {
            // Pecah instruksi berdasarkan baris baru. API biasanya menggunakan \r\n.
            // Kita juga akan menangani jika hanya \n.
            String[] steps = instructionsString.split("\\r?\\n");
            int stepNumber = 1;
            for (String stepText : steps) {
                stepText = stepText.trim(); // Hapus spasi ekstra
                // Abaikan baris kosong atau baris yang mungkin hanya nomor/simbol tanpa teks langkah yang jelas
                if (stepText.isEmpty() || stepText.matches("^\\d+\\.\\s*") || stepText.matches("^-.*") ) {
                    // Jika baris hanya nomor atau strip, mungkin tidak perlu card nomor terpisah jika sudah ada teksnya
                    // Namun, untuk konsistensi, kita akan tampilkan jika tidak kosong.
                    // Atau, Anda bisa filter lebih lanjut.
                    // Untuk sekarang, kita tampilkan semua baris yang tidak kosong setelah di-trim.
                    if(stepText.isEmpty()) continue;
                }


                LayoutInflater inflater = LayoutInflater.from(getContext());
                View stepView = inflater.inflate(R.layout.item_instruction_step, layoutInstructionsContainer, false);

                TextView textStepNumber = stepView.findViewById(R.id.text_step_number);
                TextView textStepInstruction = stepView.findViewById(R.id.text_step_instruction);

                textStepNumber.setText(String.valueOf(stepNumber));
                textStepInstruction.setText(stepText);

                layoutInstructionsContainer.addView(stepView);
                stepNumber++;
            }
        } else {
            // Tambahkan TextView sederhana jika tidak ada instruksi
            TextView noInstructionsView = new TextView(getContext());
            noInstructionsView.setText("No instructions available.");

            // --- PERBAIKAN ---
            // Gunakan style dari Material 3 yang seharusnya tersedia
            // jika library Material Components Anda versi 1.5.0 atau lebih baru (yang mendukung Material 3)
            // Jika Anda menggunakan libs.material, pastikan versinya mendukung Material 3.
            // Sebagai alternatif yang aman, Anda bisa menggunakan style bawaan Android atau AppCompat.
            // Namun, karena tema Anda adalah Material3, mari coba style Material3.
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                // Cara yang lebih modern untuk API 23+
                noInstructionsView.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            } else {
                // Fallback untuk API di bawah 23 (menggunakan AppCompat)
                androidx.core.widget.TextViewCompat.setTextAppearance(noInstructionsView, com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
            }
            // Jika com.google.android.material.R.style.TextAppearance_Material3_BodyMedium juga tidak ditemukan,
            // berarti ada masalah dengan setup library Material 3 Anda atau versinya.
            // Sebagai fallback absolut, Anda bisa menggunakan style dari AppCompat:
            // androidx.core.widget.TextViewCompat.setTextAppearance(noInstructionsView, androidx.appcompat.R.style.TextAppearance_AppCompat_Body1);
            // Atau mengatur properti secara manual:
            // noInstructionsView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14); // contoh ukuran
            // noInstructionsView.setTextColor(ContextCompat.getColor(getContext(), R.color.your_text_color)); // contoh warna

            layoutInstructionsContainer.addView(noInstructionsView);
        }


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