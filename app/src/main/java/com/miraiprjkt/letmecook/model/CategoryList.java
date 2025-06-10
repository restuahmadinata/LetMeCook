package com.miraiprjkt.letmecook.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CategoryList {
    @SerializedName("categories")
    private List<Category> categories;

    public List<Category> getCategories() {
        return categories;
    }
}