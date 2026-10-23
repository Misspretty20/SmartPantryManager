package com.example.smartpantry;

public class Units {
    public static final String[] LIST = {"g", "kg", "ml", "l", "pcs", "cup", "tbsp", "tsp"};

    public static double factor(String u) {
        switch (u) {
            case "kg": case "1": return 1000;
            case "cup": return 240;
            case "tbsp": return 15;
            case "tsp": return 5;
            default: return 1;
        }
    }

    public static String base(String u) {
        switch (u) {
            case "g": case "kg": return "g";
            case "pcs": return "pcs";
            default: return "ml";
        }
    }
}
