package com.example.pushin.database;

public class UserTable {
    //TABLE NAME
    public static final String Users = "user";
    //COLUMN NAME
    public static final String Column_userId = "userId";
    public static final String Column_userEmail = "userEmail";
    public static final String Column_userPassword = "userPassword";
    public static final String Column_userLevel = "userLevel";
    public static final String Column_userImage = "userImage";

    public static final String[] ALL_COLUMNS =
            {Column_userId, Column_userEmail, Column_userPassword, Column_userLevel, Column_userImage};

    //SQL QUERY
    public static final String SQL_CREATE =
            "CREATE TABLE " + Users + "(" +
                    Column_userId + " TEXT PRIMARY KEY," +
                    Column_userEmail + " TEXT," +
                    Column_userPassword + " TEXT," +
                    Column_userLevel + " TEXT," +
                    Column_userImage + " TEXT" + ");";
    public static final String SQL_DELETE =
            "DROP TABLE " + Users;


}
