package com.gkouskos.unipitouristapp;

public class Poi {
    public String title;
    public String description;
    public String category;
    public double latitude;
    public double longitude;

    public Poi() {};

    public Poi(String title, String description, String category, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
