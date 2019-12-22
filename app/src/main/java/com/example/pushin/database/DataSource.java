package com.example.pushin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pushin.model.DataUser;

import java.util.ArrayList;
import java.util.List;

public class DataSource {
    private Context mContext;
    private SQLiteDatabase mDatabase;
    SQLiteOpenHelper mDbHelper;

    public DataSource(Context context){
        this.mContext = context;
        mDbHelper = new DBHelper(mContext);
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public void open(){
        mDatabase = mDbHelper.getWritableDatabase();
    }

    public void close(){
        mDbHelper.close();
    }

    public DataUser createUser(DataUser user){
        ContentValues values = user.toValues();
        mDatabase.insert(UserTable.Users, null, values);
        return user;
    }

    public long getDataUserCount(){
        return DatabaseUtils.queryNumEntries(mDatabase, UserTable.Users);
    }

    public void seedDatabase(List<DataUser> dataUserList){
        long numUsers = getDataUserCount();
        if(numUsers == 0){
            for(DataUser user : dataUserList){
                try{
                    createUser(user);
                }catch (SQLiteException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public List<DataUser> getAllUser(){
        List<DataUser> dataUsers = new ArrayList<>();
        Cursor cursor = mDatabase.query(UserTable.Users, UserTable.ALL_COLUMNS,
                null,null, UserTable.Column_userEmail, null, null);
        while(cursor.moveToNext()){
            DataUser user = new DataUser();
            user.setUserId(cursor.getString(cursor.getColumnIndex(UserTable.Column_userId)));
            user.setUserEmail(cursor.getString(cursor.getColumnIndex(UserTable.Column_userEmail)));
            user.setUserPassword(cursor.getString(cursor.getColumnIndex(UserTable.Column_userPassword)));
            user.setUserLevel(cursor.getString(cursor.getColumnIndex(UserTable.Column_userLevel)));
            user.setUserImage(cursor.getString(cursor.getColumnIndex(UserTable.Column_userImage)));
            dataUsers.add(user);
        }
        return dataUsers;
    }

}
