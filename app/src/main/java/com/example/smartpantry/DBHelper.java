package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "pantry.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, " +
                "qty REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, qty REAL NOT NULL, unit TEXT NOT NULL, FOREIGN KEY(recipe_id) REFERENCES recipes(id))");
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }

    public long addItem(String name, double qty, String unit, String expiry) {
        return getWritableDatabase().insert("pantry", null, values(name, qty, unit, expiry));
    }

    public int updateItem(long id, String name, double qty, String unit, String expiry) {
        return getWritableDatabase().update("pantry", values(name, qty, unit, expiry), "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteItem(long id) {
        return getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)});
    }

    public List<PantryItem> getAllItems() {
        List<PantryItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,qty,unit,expiry FROM pantry ORDER BY name", null)) {
            while (c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    public PantryItem getItem(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,name,qty,unit,expiry FROM pantry WHERE id=?",
                new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    private ContentValues values(String name, double qty, String unit, String expiry) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("qty", qty);
        v.put("unit", unit);
        v.put("expiry", expiry);
        return v;
    }

    private PantryItem fromCursor(Cursor c) {
        String exp = c.getString(4);
        return new PantryItem(c.getLong(0), c.getString(1), c.getDouble(2), c.getString(3), exp == null ? "" : exp);
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id,name,steps FROM recipes ORDER BY name", null)) {
            while (c.moveToNext()) {
                Recipe r = new Recipe(c.getLong(0), c.getString(1), c.getString(2));
                r.ingredients = getIngredients(db, r.id);
                list.add(r);
            }
        }
        return list;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id,name,steps FROM recipes WHERE id=?", new String[]{String.valueOf(id)})) {
            if (!c.moveToFirst()) return null;
            Recipe r = new Recipe(c.getLong(0), c.getString(1), c.getString(2));
            r.ingredients = getIngredients(db, r.id);
            return r;
        }
    }

    private List<RecipeIngredient> getIngredients(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        try (Cursor c = db.rawQuery("SELECT name,qty,unit FROM recipe_ingredients WHERE recipe_id=?",
                new String[]{String.valueOf(recipeId)})) {
            while (c.moveToNext()) list.add(new RecipeIngredient(c.getString(0), c.getDouble(1), c.getString(2)));
        }
        return list;
    }

    private void seed(SQLiteDatabase db, String name, String steps, String... ings) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("steps", steps);
        long id = db.insert("recipes", null, v);
        for (String s : ings) {
            String[] p = s.split("\\|");
            ContentValues c = new ContentValues();
            c.put("recipe_id", id);
            c.put("name", p[0]);
            c.put("qty", Double.parseDouble(p[1]));
            c.put("unit", p[2]);
            db.insert("recipe_ingredients", null, c);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        seed(db, "Scrambled Eggs", "1. Beat the eggs with salt.\n2. Melt butter in a pan.\n3. Pour in eggs and stir gently until just set.",
                "egg|2|pcs", "butter|10|g", "salt|1|tsp");
        seed(db, "Cheese Omelette", "1. Beat eggs with salt.\n2. Cook in buttered pan.\n3. Add cheese, fold and serve.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g", "salt|1|tsp");
        seed(db, "Tomato Pasta", "1. Boil pasta.\n2. Fry onion and garlic in oil.\n3. Add chopped tomatoes and simmer 10 min.\n4. Mix with pasta, season with salt.",
                "pasta|200|g", "tomato|4|pcs", "onion|1|pcs", "garlic|2|pcs", "olive oil|2|tbsp", "salt|1|tsp");
        seed(db, "Egg Fried Rice", "1. Fry onion in oil.\n2. Add cooked rice and soy sauce.\n3. Push aside, scramble eggs, mix everything.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");
        seed(db, "Simple Pancakes", "1. Whisk flour, milk, egg and sugar.\n2. Melt butter in a pan.\n3. Fry spoonfuls until golden on both sides.",
                "flour|200|g", "milk|300|ml", "egg|1|pcs", "sugar|1|tbsp", "butter|20|g");
        seed(db, "Cheese Toast", "1. Butter the bread.\n2. Top with cheese.\n3. Grill until melted and golden.",
                "bread|2|pcs", "cheese|40|g", "butter|10|g");
        seed(db, "Mashed Potatoes", "1. Peel and boil potatoes until soft.\n2. Drain and mash with butter and milk.\n3. Season with salt.",
                "potato|500|g", "butter|30|g", "milk|50|ml", "salt|1|tsp");
        seed(db, "Chicken Rice Bowl", "1. Cook rice.\n2. Fry onion and chicken pieces in oil.\n3. Season with salt and serve over rice.",
                "chicken|300|g", "rice|200|g", "onion|1|pcs", "salt|1|tsp", "oil|1|tbsp");
        seed(db, "Garlic Bread", "1. Mash crushed garlic into butter.\n2. Spread on bread.\n3. Bake at 180C for 10 minutes.",
                "bread|4|pcs", "garlic|2|pcs", "butter|30|g");
        seed(db, "Vegetable Soup", "1. Chop carrots, potatoes and onion.\n2. Fry onion in oil, add vegetables and water.\n3. Simmer 25 min, season with salt.",
                "carrot|2|pcs", "potato|2|pcs", "onion|1|pcs", "salt|1|tsp", "oil|1|tbsp");
        seed(db, "Egg Sandwich", "1. Fry or boil the eggs and season with salt.\n2. Butter the bread.\n3. Fill sandwich with egg.",
                "bread|2|pcs", "egg|2|pcs", "butter|10|g", "salt|1|tsp");
        seed(db, "Banana Pancakes", "1. Mash bananas.\n2. Mix with eggs and flour.\n3. Fry small pancakes until golden.",
                "banana|2|pcs", "egg|2|pcs", "flour|50|g");
        seed(db, "Tuna Pasta", "1. Boil pasta.\n2. Fry onion in olive oil.\n3. Add tuna, then toss with pasta.",
                "pasta|200|g", "tuna|1|pcs", "onion|1|pcs", "olive oil|1|tbsp");
        seed(db, "Rice Pudding", "1. Simmer rice in milk on low heat for 30 min, stirring.\n2. Add sugar and serve warm.",
                "rice|100|g", "milk|500|ml", "sugar|2|tbsp");
        seed(db, "Baked Potato", "1. Prick potatoes and bake at 200C for 1 hour.\n2. Cut open, add butter, cheese and salt.",
                "potato|2|pcs", "butter|20|g", "cheese|30|g", "salt|1|tsp");
        seed(db, "French Toast", "1. Whisk eggs, milk and sugar.\n2. Soak bread slices.\n3. Fry until golden.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp");
        seed(db, "Tomato Soup", "1. Fry onion and garlic in oil.\n2. Add chopped tomatoes, simmer 20 min.\n3. Blend, season with salt.",
                "tomato|6|pcs", "onion|1|pcs", "garlic|2|pcs", "oil|1|tbsp", "salt|1|tsp");
        seed(db, "Beans on Toast", "1. Toast the bread and butter it.\n2. Heat the beans.\n3. Pour over toast.",
                "baked beans|200|g", "bread|2|pcs", "butter|10|g");
    }
}



