package com.example.leisuremap;

public class Object {
    private String name;
    private double distance;
    private double lat;
    private double lon;
    private int rating;
    private String city;
    private String type;
    private double score;
    private String distanceString;

    public Object(String name, double distance, double lat, double lon, int rating, String city, String type, double score) {
        this.name = name;
        this.distance = distance;
        this.lat = lat;
        this.lon = lon;
        this.rating = rating;
        this.city = city;
        this.type = type;
        this.score = score;
    }

    public Object(String name, double lat, double lon, String type, String distanceString) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.type = type;
        this.distanceString = distanceString;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDistanceString() {
        return distanceString;
    }

    public void setDistanceString(String distanceString) {
        this.distanceString = distanceString;
    }
}
