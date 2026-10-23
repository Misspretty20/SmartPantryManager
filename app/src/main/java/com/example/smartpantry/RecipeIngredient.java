package com.example.smartpantry;

public class RecipeIngredient {
    public String name;
    public double qty;
    public String unit;


    public RecipeIngredient(String name, double qty, String unit) {
        this.name = name; this.qty = qty; this.unit = unit;
    }
}
