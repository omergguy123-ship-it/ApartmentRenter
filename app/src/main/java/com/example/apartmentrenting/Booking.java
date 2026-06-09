package com.example.apartmentrenting;

/**
 * מחלקת מודל (Model) המייצגת הזמנה (Booking) במערכת.
 * כל הזמנה מקשרת בין שוכר (renter) לנכס (listing) ולמארח (host).
 * המחלקה משמשת גם לשמירה ב-Cloud Firestore וגם להצגה ב-RecyclerView.
 *
 * ערכי status האפשריים:
 *   "PENDING"  - הבקשה נשלחה וממתינה לאישור המארח
 *   "APPROVED" - המארח אישר את הבקשה
 *   "DECLINED" - המארח דחה את הבקשה
 */
public class Booking {
    // מזהה ייחודי של ההזמנה ב-Firestore
    private String bookingId;
    // מזהה הנכס שהוזמן (document ID ב-listings)
    private String listingId;
    // UID של השוכר (Firebase Auth UID)
    private String renterUid;
    // שם מלא של השוכר (firstName + lastName) לתצוגה אצל המארח
    private String renterName;
    // כותרת הנכס לתצוגה בכרטיסי ההזמנה
    private String propertyTitle;
    // URL של תמונת הנכס (Firebase Storage או Unsplash)
    private String imageUrl;
    // מיקום הנכס
    private String location;
    // מחיר הנכס ללילה בדולרים
    private double price;
    // חותמת זמן (Unix timestamp) של מועד שליחת ההזמנה
    private long bookingDate;
    // סטטוס ההזמנה: "PENDING" / "APPROVED" / "DECLINED"
    private String status;
    // תאריך כניסה מרוצה בפורמט "YYYY-MM-DD" (לדוגמה: "2026-06-12")
    private String checkInDate;
    // תאריך עזיבה מרוצה בפורמט "YYYY-MM-DD" (לדוגמה: "2026-06-18")
    private String checkOutDate;
    // הערה חופשית שהשוכר כותב למארח עם הגשת הבקשה
    private String note;
    // UID של המארח (Firebase Auth UID) - לשאילתת הבקשות של המארח
    private String hostUid;

    /**
     * בנאי ריק (No-arg constructor) - נדרש על ידי Firestore לבצע
     * deserialization אוטומטי ממסמכי Firestore לאובייקטי Java.
     */
    public Booking() {
    }

    /**
     * בנאי מלא ליצירת אובייקט Booking חדש עם כל הפרמטרים.
     * נקרא ב-HouseDetailActivity בעת שליחת בקשת הזמנה חדשה.
     */
    public Booking(String bookingId, String listingId, String renterUid, String renterName, String propertyTitle,
                   String imageUrl, String location, double price, long bookingDate, String status,
                   String checkInDate, String checkOutDate, String note, String hostUid) {
        this.bookingId = bookingId;
        this.listingId = listingId;
        this.renterUid = renterUid;
        this.renterName = renterName;
        this.propertyTitle = propertyTitle;
        this.imageUrl = imageUrl;
        this.location = location;
        this.price = price;
        this.bookingDate = bookingDate;
        this.status = status;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.note = note;
        this.hostUid = hostUid;
    }

    // --- Getters ו-Setters ---
    // נדרשים על ידי Firestore לקריאה וכתיבה של שדות האובייקט

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getRenterUid() {
        return renterUid;
    }

    public void setRenterUid(String renterUid) {
        this.renterUid = renterUid;
    }

    public String getRenterName() {
        return renterName;
    }

    public void setRenterName(String renterName) {
        this.renterName = renterName;
    }

    public String getPropertyTitle() {
        return propertyTitle;
    }

    public void setPropertyTitle(String propertyTitle) {
        this.propertyTitle = propertyTitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public long getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(long bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getHostUid() {
        return hostUid;
    }

    public void setHostUid(String hostUid) {
        this.hostUid = hostUid;
    }
}
