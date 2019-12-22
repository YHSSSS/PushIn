package com.example.pushin.model;

import android.content.ContentValues;

import com.example.pushin.database.UserTable;

import java.util.UUID;

public class DataUser {

    private String userId;
    private String userEmail;
    private String userPassword;
    private String userLevel;
    private String userImage;

    public DataUser() {
    }

    public DataUser(String userId, String userEmail, String userPassword, String userLevel, String userImage) {
        if(userId == null) userId = UUID.randomUUID().toString();

        this.userId = userId;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userLevel = userLevel;
        this.userImage = userImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public ContentValues toValues(){
        ContentValues values = new ContentValues(7);
        values.put(UserTable.Column_userId, userId);
        values.put(UserTable.Column_userEmail, userEmail);
        values.put(UserTable.Column_userPassword, userPassword);
        values.put(UserTable.Column_userLevel, userLevel);
        values.put(UserTable.Column_userImage, userImage);
        return values;
    }

    @Override
    public String toString() {
        return "DataUser{" +
                "userId='" + userId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPassword='" + userPassword + '\'' +
                ", userLevel='" + userLevel + '\'' +
                ", userImage='" + userImage + '\'' +
                '}';
    }

}
