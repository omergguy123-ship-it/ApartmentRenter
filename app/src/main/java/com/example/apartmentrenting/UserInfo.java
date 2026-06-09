package com.example.apartmentrenting;

/**
 * UserInfo - מחלקת מודל (Model) המייצגת פרטי משתמש רשום במערכת.
 * נשמרת ב-Firestore תחת אוסף "users" עם ה-UID של המשתמש כמזהה המסמך.
 *
 * המחלקה משמשת ב-SignUp.java לשמירת פרטי משתמש חדש לאחר ההרשמה.
 * ב-BrouseHosesOrUploadListing ו-UploadHouseActivity נקראת לשליפת שם המשתמש.
 */
public class UserInfo {
    // שם פרטי של המשתמש
    private String firstName;
    // שם משפחה של המשתמש
    private String lastName;
    // UID ייחודי של המשתמש ב-Firebase Authentication (נשמר גם בתוך המסמך)
    private String userID;

    /**
     * בנאי ריק (No-arg constructor) - נדרש על ידי Firestore לביצוע
     * deserialization אוטומטי ממסמכי Firestore לאובייקטי Java.
     */
    public UserInfo() {
    }

    /**
     * בנאי מלא - נקרא ב-SignUp.java לאחר יצירת חשבון ב-Firebase Auth.
     *
     * @param firstName שם פרטי
     * @param lastName  שם משפחה
     * @param userID    ה-UID מ-Firebase Authentication
     */
    public UserInfo(String firstName, String lastName, String userID) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userID = userID;
    }

    // --- Getters ו-Setters ---
    // נדרשים על ידי Firestore לקריאה וכתיבה אוטומטית של שדות האובייקט

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
