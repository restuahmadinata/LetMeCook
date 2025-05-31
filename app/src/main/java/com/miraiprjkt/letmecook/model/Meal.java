// app/src/main/java/com/miraiprjkt/letmecook/model/Meal.java
package com.miraiprjkt.letmecook.model;

import com.google.gson.annotations.SerializedName;

public class Meal {
    @SerializedName("idMeal")
    private String idMeal;

    @SerializedName("strMeal")
    private String strMeal;

    @SerializedName("strMealThumb")
    private String strMealThumb;

    // Getter methods
    public String getIdMeal() { return idMeal; }
    public String getStrMeal() { return strMeal; }
    public String getStrMealThumb() { return strMealThumb; }

    // Anda bisa menambahkan field lain jika perlu (misalnya strInstructions, strIngredient1, dll.)
    // jika Anda ingin menampilkan lebih banyak detail langsung di daftar atau meneruskannya.
}