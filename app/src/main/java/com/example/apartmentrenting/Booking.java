package com.example.apartmentrenting;

public class Booking {
    private String bookingId;
    private String listingId;
    private String renterUid;
    private String renterName;
    private String propertyTitle;
    private String imageUrl;
    private String location;
    private double price;
    private long bookingDate;
    private String status;       // "PENDING", "APPROVED", "DECLINED"
    private String checkInDate;  // e.g. "2026-06-12"
    private String checkOutDate; // e.g. "2026-06-18"
    private String note;         // Custom text note to host
    private String hostUid;      // Owner's UID

    // No-arg constructor required for Firestore
    public Booking() {
    }

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
