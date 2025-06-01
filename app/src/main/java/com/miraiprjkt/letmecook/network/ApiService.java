// app/src/main/java/com/miraiprjkt/letmecook/network/ApiService.java
package com.miraiprjkt.letmecook.network;

import com.miraiprjkt.letmecook.model.CategoryList;
import com.miraiprjkt.letmecook.model.MealList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/json/v1/1/random.php")
    Call<MealList> getRandomMeal();

    @GET("api/json/v1/1/categories.php")
    Call<CategoryList> getCategories();

    @GET("api/json/v1/1/search.php")
    Call<MealList> searchMeals(@Query("s") String query);

    @GET("api/json/v1/1/filter.php")
    Call<MealList> filterByCategory(@Query("c") String category);

    // Endpoint untuk "semua" resep (misalnya, berdasarkan huruf pertama)
    @GET("api/json/v1/1/search.php")
    Call<MealList> listMealsByFirstLetter(@Query("f") String firstLetter);

    @GET("api/json/v1/1/lookup.php")
    Call<MealList> getMealDetails(@Query("i") String mealId);

}