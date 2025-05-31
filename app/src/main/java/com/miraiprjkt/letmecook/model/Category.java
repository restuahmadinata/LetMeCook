// app/src/main/java/com/miraiprjkt/letmecook/model/Category.java
package com.miraiprjkt.letmecook.model;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("idCategory")
    private String idCategory;

    @SerializedName("strCategory")
    private String strCategory;

    @SerializedName("strCategoryThumb")
    private String strCategoryThumb;

    @SerializedName("strCategoryDescription")
    private String strCategoryDescription;

    // Getter methods
    public String getIdCategory() { return idCategory; }
    public String getStrCategory() { return strCategory; }
    public String getStrCategoryThumb() { return strCategoryThumb; }
    public String getStrCategoryDescription() { return strCategoryDescription; }
}