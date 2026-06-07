package com.example.apartmentrenting;

public class HouseListing {
    private String listingId;
    private String title;
    private String description;
    private String location;
    private double price;
    private String imageUrl;
    private String hostUid;
    private String hostName;
    private String category;
    private float rating;
    private int beds;
    private int baths;
    private boolean wifi;
    private boolean ac;
    private boolean kitchen;
    private boolean parking;

    // Default constructor required for Firestore
    public HouseListing() {
    }

    public HouseListing(String listingId, String title, String description, String location, double price, 
                        String imageUrl, String hostUid, String hostName, String category, float rating, 
                        int beds, int baths, boolean wifi, boolean ac, boolean kitchen, boolean parking) {
        this.listingId = listingId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.price = price;
        this.imageUrl = imageUrl;
        this.hostUid = hostUid;
        this.hostName = hostName;
        this.category = category;
        this.rating = rating;
        this.beds = beds;
        this.baths = baths;
        this.wifi = wifi;
        this.ac = ac;
        this.kitchen = kitchen;
        this.parking = parking;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getHostUid() {
        return hostUid;
    }

    public void setHostUid(String hostUid) {
        this.hostUid = hostUid;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getBeds() {
        return beds;
    }

    public void setBeds(int beds) {
        this.beds = beds;
    }

    public int getBaths() {
        return baths;
    }

    public void setBaths(int baths) {
        this.baths = baths;
    }

    public boolean isWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public boolean isAc() {
        return ac;
    }

    public void setAc(boolean ac) {
        this.ac = ac;
    }

    public boolean isKitchen() {
        return kitchen;
    }

    public void setKitchen(boolean kitchen) {
        this.kitchen = kitchen;
    }

    public boolean isParking() {
        return parking;
    }

    public void setParking(boolean parking) {
        this.parking = parking;
    }
}
