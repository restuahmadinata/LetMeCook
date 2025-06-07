package com.miraiprjkt.letmecook.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.miraiprjkt.letmecook.R;
import com.miraiprjkt.letmecook.RecipeDetailActivity;
import com.miraiprjkt.letmecook.model.Meal;

import java.util.List;

public class FavoriteRecipeAdapter extends RecyclerView.Adapter<FavoriteRecipeAdapter.ViewHolder> {

    private Context context;
    private List<Meal> favoriteMeals;

    public FavoriteRecipeAdapter(Context context, List<Meal> favoriteMeals) {
        this.context = context;
        this.favoriteMeals = favoriteMeals;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Meal meal = favoriteMeals.get(position);
        holder.recipeName.setText(meal.getStrMeal());
        Glide.with(context).load(meal.getStrMealThumb()).into(holder.recipeImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_MEAL_ID, meal.getIdMeal());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteMeals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.image_recipe);
            recipeName = itemView.findViewById(R.id.text_recipe_name);
        }
    }
}