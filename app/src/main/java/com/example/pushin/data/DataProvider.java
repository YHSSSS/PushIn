package com.example.pushin.data;

import com.example.pushin.database.DataSource;
import com.example.pushin.model.DataUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProvider {
    public static List<DataUser> dataUserList;
    public static Map<String, DataUser> dataUserMap;

    static{
        dataUserList = new ArrayList<>();
        dataUserMap = new HashMap<>();

        addUser(new DataUser(null, "Yellowow@outlook.com",
                "SecurePassword", "B4", "image1.jpg"));
        addUser(new DataUser(null, "1739534664@qq.com",
                "SecurePassword", "B3", "image2.jpg"));
        addUser(new DataUser(null, "jphuang@dundee.com",
                "SecurePassword", "B1", "image3.jpg"));
        addUser(new DataUser(null, "jdfavadfga@outlook.com",
                "SecurePassword", "B1", "image4.jpg"));
        addUser(new DataUser(null, "jcasdg@dundee.com",
                "SecurePassword", "B4", "image5.jpg"));
        addUser(new DataUser(null, "jdsvvvx@dundee.com",
                "SecurePassword", "M", "image6.jpg"));
        addUser(new DataUser(null, "aERGDFGg@qq.com",
                "SecurePassword", "B4", "image7.jpg"));
        addUser(new DataUser(null, "jdftbsdfg@outlook.com",
                "SecurePassword", "M", "image8.jpg"));
        addUser(new DataUser(null, "jdftbsaddfg@outlook.com",
                "SecurePassword", "B2", "image9.jpg"));
    }


    private static void addUser(DataUser dataUser) {
        dataUserList.add(dataUser);
        dataUserMap.put(dataUser.getUserId(), dataUser);
    }


}
