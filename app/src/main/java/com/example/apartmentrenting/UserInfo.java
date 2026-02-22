package com.example.apartmentrenting;

public class UserInfo {
    private String FirstName,LastName,UserID;


    public UserInfo(String firstName, String lastName, String userID) {
        FirstName = firstName;
        LastName = lastName;
        UserID = userID;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getUserID() {
        return UserID;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }




}
