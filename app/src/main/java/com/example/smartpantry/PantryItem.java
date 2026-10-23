package com.example.smartpantry;


public class PantryItem {
    public long id;
    public String name;
    public double qty;
    public String unit;
    public String expiry;

    public PantryItem(long id, String name, double qty, String unit, String expiry) {
        this.id = id; this.name = name; this.qty = qty; this.unit = unit; this.expiry = expiry;
    }
}