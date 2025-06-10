package com.miraiprjkt.letmecook.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.miraiprjkt.letmecook.model.Meal;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "letmecook_favorites.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MEAL_ID = "meal_id";
    public static final String COLUMN_MEAL_NAME = "meal_name";
    public static final String COLUMN_MEAL_THUMB = "meal_thumb";
    public static final String COLUMN_MEAL_CATEGORY = "meal_category";
    public static final String COLUMN_MEAL_AREA = "meal_area";
    public static final String COLUMN_MEAL_INSTRUCTIONS = "meal_instructions";
    public static final String COLUMN_MEAL_TAGS = "meal_tags";
    public static final String COLUMN_MEAL_YOUTUBE = "meal_youtube";
    public static final String COLUMN_MEAL_SOURCE = "meal_source";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder createTableQueryBuilder = new StringBuilder();
        createTableQueryBuilder.append("CREATE TABLE ").append(TABLE_FAVORITES).append(" (")
                .append(COLUMN_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(COLUMN_MEAL_ID).append(" TEXT UNIQUE, ")
                .append(COLUMN_MEAL_NAME).append(" TEXT, ")
                .append(COLUMN_MEAL_THUMB).append(" TEXT, ")
                .append(COLUMN_MEAL_CATEGORY).append(" TEXT, ")
                .append(COLUMN_MEAL_AREA).append(" TEXT, ")
                .append(COLUMN_MEAL_INSTRUCTIONS).append(" TEXT, ")
                .append(COLUMN_MEAL_TAGS).append(" TEXT, ")
                .append(COLUMN_MEAL_YOUTUBE).append(" TEXT, ")
                .append(COLUMN_MEAL_SOURCE).append(" TEXT");

        for (int i = 1; i <= 20; i++) {
            createTableQueryBuilder.append(", strIngredient").append(i).append(" TEXT");
            createTableQueryBuilder.append(", strMeasure").append(i).append(" TEXT");
        }
        createTableQueryBuilder.append(")");

        db.execSQL(createTableQueryBuilder.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    public void addFavorite(Meal meal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEAL_ID, meal.getIdMeal());
        values.put(COLUMN_MEAL_NAME, meal.getStrMeal());
        values.put(COLUMN_MEAL_THUMB, meal.getStrMealThumb());
        values.put(COLUMN_MEAL_CATEGORY, meal.getStrCategory());
        values.put(COLUMN_MEAL_AREA, meal.getStrArea());
        values.put(COLUMN_MEAL_INSTRUCTIONS, meal.getStrInstructions());
        values.put(COLUMN_MEAL_TAGS, meal.getStrTags());
        values.put(COLUMN_MEAL_YOUTUBE, meal.getStrYoutube());
        values.put(COLUMN_MEAL_SOURCE, meal.getStrSource());

        values.put("strIngredient1", meal.getStrIngredient1());
        values.put("strMeasure1", meal.getStrMeasure1());
        values.put("strIngredient2", meal.getStrIngredient2());
        values.put("strMeasure2", meal.getStrMeasure2());
        values.put("strIngredient3", meal.getStrIngredient3());
        values.put("strMeasure3", meal.getStrMeasure3());
        values.put("strIngredient4", meal.getStrIngredient4());
        values.put("strMeasure4", meal.getStrMeasure4());
        values.put("strIngredient5", meal.getStrIngredient5());
        values.put("strMeasure5", meal.getStrMeasure5());
        values.put("strIngredient6", meal.getStrIngredient6());
        values.put("strMeasure6", meal.getStrMeasure6());
        values.put("strIngredient7", meal.getStrIngredient7());
        values.put("strMeasure7", meal.getStrMeasure7());
        values.put("strIngredient8", meal.getStrIngredient8());
        values.put("strMeasure8", meal.getStrMeasure8());
        values.put("strIngredient9", meal.getStrIngredient9());
        values.put("strMeasure9", meal.getStrMeasure9());
        values.put("strIngredient10", meal.getStrIngredient10());
        values.put("strMeasure10", meal.getStrMeasure10());
        values.put("strIngredient11", meal.getStrIngredient11());
        values.put("strMeasure11", meal.getStrMeasure11());
        values.put("strIngredient12", meal.getStrIngredient12());
        values.put("strMeasure12", meal.getStrMeasure12());
        values.put("strIngredient13", meal.getStrIngredient13());
        values.put("strMeasure13", meal.getStrMeasure13());
        values.put("strIngredient14", meal.getStrIngredient14());
        values.put("strMeasure14", meal.getStrMeasure14());
        values.put("strIngredient15", meal.getStrIngredient15());
        values.put("strMeasure15", meal.getStrMeasure15());
        values.put("strIngredient16", meal.getStrIngredient16());
        values.put("strMeasure16", meal.getStrMeasure16());
        values.put("strIngredient17", meal.getStrIngredient17());
        values.put("strMeasure17", meal.getStrMeasure17());
        values.put("strIngredient18", meal.getStrIngredient18());
        values.put("strMeasure18", meal.getStrMeasure18());
        values.put("strIngredient19", meal.getStrIngredient19());
        values.put("strMeasure19", meal.getStrMeasure19());
        values.put("strIngredient20", meal.getStrIngredient20());
        values.put("strMeasure20", meal.getStrMeasure20());

        db.insert(TABLE_FAVORITES, null, values);
        db.close();
    }

    public void removeFavorite(String mealId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, COLUMN_MEAL_ID + " = ?", new String[]{mealId});
        db.close();
    }

    public boolean isFavorite(String mealId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_MEAL_ID},
                COLUMN_MEAL_ID + " = ?", new String[]{mealId}, null, null, null);
        boolean isFavorite = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isFavorite;
    }

    public List<Meal> getAllFavorites() {
        List<Meal> mealList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAVORITES, null);

        if (cursor.moveToFirst()) {
            do {
                Meal meal = new Meal();
                meal.setIdMeal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_ID)));
                meal.setStrMeal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_NAME)));
                meal.setStrMealThumb(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_THUMB)));
                meal.setStrCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_CATEGORY)));
                meal.setStrArea(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_AREA)));
                meal.setStrInstructions(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_INSTRUCTIONS)));
                meal.setStrTags(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_TAGS)));
                meal.setStrYoutube(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_YOUTUBE)));
                meal.setStrSource(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_SOURCE)));

                meal.setStrIngredient1(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient1")));
                meal.setStrMeasure1(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure1")));
                meal.setStrIngredient2(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient2")));
                meal.setStrMeasure2(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure2")));
                meal.setStrIngredient3(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient3")));
                meal.setStrMeasure3(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure3")));
                meal.setStrIngredient4(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient4")));
                meal.setStrMeasure4(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure4")));
                meal.setStrIngredient5(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient5")));
                meal.setStrMeasure5(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure5")));
                meal.setStrIngredient6(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient6")));
                meal.setStrMeasure6(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure6")));
                meal.setStrIngredient7(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient7")));
                meal.setStrMeasure7(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure7")));
                meal.setStrIngredient8(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient8")));
                meal.setStrMeasure8(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure8")));
                meal.setStrIngredient9(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient9")));
                meal.setStrMeasure9(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure9")));
                meal.setStrIngredient10(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient10")));
                meal.setStrMeasure10(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure10")));
                meal.setStrIngredient11(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient11")));
                meal.setStrMeasure11(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure11")));
                meal.setStrIngredient12(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient12")));
                meal.setStrMeasure12(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure12")));
                meal.setStrIngredient13(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient13")));
                meal.setStrMeasure13(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure13")));
                meal.setStrIngredient14(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient14")));
                meal.setStrMeasure14(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure14")));
                meal.setStrIngredient15(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient15")));
                meal.setStrMeasure15(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure15")));
                meal.setStrIngredient16(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient16")));
                meal.setStrMeasure16(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure16")));
                meal.setStrIngredient17(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient17")));
                meal.setStrMeasure17(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure17")));
                meal.setStrIngredient18(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient18")));
                meal.setStrMeasure18(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure18")));
                meal.setStrIngredient19(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient19")));
                meal.setStrMeasure19(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure19")));
                meal.setStrIngredient20(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient20")));
                meal.setStrMeasure20(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure20")));

                mealList.add(meal);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return mealList;
    }

    public Meal getFavoriteMealById(String mealId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, null, // null selects all columns
                COLUMN_MEAL_ID + " = ?", new String[]{mealId}, null, null, null);

        Meal meal = null;
        if (cursor != null && cursor.moveToFirst()) {
            meal = new Meal();
            meal.setIdMeal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_ID)));
            meal.setStrMeal(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_NAME)));
            meal.setStrMealThumb(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_THUMB)));
            meal.setStrCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_CATEGORY)));
            meal.setStrArea(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_AREA)));
            meal.setStrInstructions(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_INSTRUCTIONS)));
            meal.setStrTags(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_TAGS)));
            meal.setStrYoutube(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_YOUTUBE)));
            meal.setStrSource(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_SOURCE)));

            meal.setStrIngredient1(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient1")));
            meal.setStrMeasure1(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure1")));
            meal.setStrIngredient2(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient2")));
            meal.setStrMeasure2(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure2")));
            meal.setStrIngredient3(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient3")));
            meal.setStrMeasure3(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure3")));
            meal.setStrIngredient4(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient4")));
            meal.setStrMeasure4(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure4")));
            meal.setStrIngredient5(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient5")));
            meal.setStrMeasure5(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure5")));
            meal.setStrIngredient6(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient6")));
            meal.setStrMeasure6(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure6")));
            meal.setStrIngredient7(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient7")));
            meal.setStrMeasure7(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure7")));
            meal.setStrIngredient8(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient8")));
            meal.setStrMeasure8(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure8")));
            meal.setStrIngredient9(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient9")));
            meal.setStrMeasure9(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure9")));
            meal.setStrIngredient10(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient10")));
            meal.setStrMeasure10(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure10")));
            meal.setStrIngredient11(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient11")));
            meal.setStrMeasure11(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure11")));
            meal.setStrIngredient12(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient12")));
            meal.setStrMeasure12(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure12")));
            meal.setStrIngredient13(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient13")));
            meal.setStrMeasure13(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure13")));
            meal.setStrIngredient14(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient14")));
            meal.setStrMeasure14(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure14")));
            meal.setStrIngredient15(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient15")));
            meal.setStrMeasure15(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure15")));
            meal.setStrIngredient16(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient16")));
            meal.setStrMeasure16(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure16")));
            meal.setStrIngredient17(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient17")));
            meal.setStrMeasure17(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure17")));
            meal.setStrIngredient18(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient18")));
            meal.setStrMeasure18(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure18")));
            meal.setStrIngredient19(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient19")));
            meal.setStrMeasure19(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure19")));
            meal.setStrIngredient20(cursor.getString(cursor.getColumnIndexOrThrow("strIngredient20")));
            meal.setStrMeasure20(cursor.getString(cursor.getColumnIndexOrThrow("strMeasure20")));

            cursor.close();
        }
        db.close();
        return meal;
    }
}
