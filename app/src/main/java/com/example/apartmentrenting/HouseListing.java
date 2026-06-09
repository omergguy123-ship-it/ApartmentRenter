package com.example.apartmentrenting;

/**
 * מחלקת מודל (Model) המייצגת מודעת נכס להשכרה (HouseListing) במערכת.
 * כל נכס מכיל פרטים כגון כותרת, תיאור, מחיר, שירותים (Amenities), ומזהה המארח.
 * המחלקה משמשת לשמירה ב-Cloud Firestore וגם לתצוגה ב-RecyclerView של רשימת הנכסים.
 *
 * Firestore ממפה את שמות השדות באופן אוטומטי לפי שמות הפונקציות get/set (camelCase).
 */
public class HouseListing {
    // מזהה ייחודי של הנכס - נשמר נפרד מהמסמך ב-Firestore (מוגדר ידנית ב-setListingId)
    private String listingId;
    // כותרת הנכס (לדוגמה: "Grand Oceanview Villa")
    private String title;
    // תיאור מפורט של הנכס
    private String description;
    // עיר/מיקום הנכס (לדוגמה: "Malibu, California")
    private String location;
    // מחיר ללילה בדולרים
    private double price;
    // URL של תמונת הנכס (Firebase Storage URL או Unsplash URL)
    private String imageUrl;
    // UID של המארח (Firebase Auth UID) - משמש למניעת הזמנה עצמית
    private String hostUid;
    // שם המארח לתצוגה (firstName + lastName)
    private String hostName;
    // קטגוריה של הנכס: "Apartment" / "Villa" / "Cabin" / "Studio"
    private String category;
    // דירוג הנכס (1.0 עד 5.0)
    private float rating;
    // מספר חדרי שינה
    private int beds;
    // מספר חדרי אמבטיה
    private int baths;
    // האם יש Wi-Fi בנכס
    private boolean wifi;
    // האם יש מיזוג אוויר בנכס
    private boolean ac;
    // האם יש מטבח בנכס
    private boolean kitchen;
    // האם יש חניה חינמית בנכס
    private boolean parking;

    /**
     * בנאי ריק (No-arg constructor) - נדרש על ידי Firestore לבצע
     * deserialization אוטומטי ממסמכי Firestore לאובייקטי Java.
     */
    public HouseListing() {
    }

    /**
     * בנאי מלא ליצירת אובייקט HouseListing חדש עם כל הפרמטרים.
     * נקרא ב-UploadHouseActivity ובפונקציית seedMockDataIfCollectionEmpty.
     */
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

    // --- Getters ו-Setters ---
    // נדרשים על ידי Firestore לקריאה וכתיבה של שדות האובייקט

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
