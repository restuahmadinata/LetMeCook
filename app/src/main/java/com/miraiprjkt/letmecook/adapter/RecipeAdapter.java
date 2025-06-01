// app/src/main/java/com/miraiprjkt/letmecook/adapter/RecipeAdapter.java
package com.miraiprjkt.letmecook.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.miraiprjkt.letmecook.R;
import com.miraiprjkt.letmecook.model.Meal;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private Context context;
    private List<Meal> mealList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Meal meal);
    }

    public RecipeAdapter(Context context, List<Meal> mealList, OnItemClickListener listener) {
        this.context = context;
        this.mealList = mealList;
        this.listener = listener; // Listener ini sekarang akan diimplementasikan di HomeFragment
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        holder.bind(meal, listener);
    }

    @Override
    public int getItemCount() {
        return mealList == null ? 0 : mealList.size();
    }

    public void updateData(List<Meal> newMealList) {
        this.mealList = newMealList;
        notifyDataSetChanged(); // Atau gunakan DiffUtil untuk performa lebih baik
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView imageRecipe;
        TextView textRecipeName;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRecipe = itemView.findViewById(R.id.image_recipe);
            textRecipeName = itemView.findViewById(R.id.text_recipe_name);
        }

        public void bind(final Meal meal, final OnItemClickListener listener) {
            textRecipeName.setText(meal.getStrMeal());
            Glide.with(itemView.getContext())
                    .load(meal.getStrMealThumb())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageRecipe);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(meal); // Panggil listener yang diteruskan dari Fragment
                }
            });
        }
    }
}