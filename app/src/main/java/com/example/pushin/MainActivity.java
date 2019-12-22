package com.example.pushin;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pushin.data.DataProvider;
import com.example.pushin.database.DataSource;
import com.example.pushin.model.DataUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //TextView tvOut;
    DataSource mDataSource;
    List<DataUser> dataUserList = DataProvider.dataUserList;
    //List<String> userEmail = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDataSource = new DataSource(this);
        mDataSource.open();
        mDataSource.seedDatabase(dataUserList);//retrieve data

        List<DataUser> listFromDB = mDataSource.getAllUser();
        DataUserAdapter adapter = new DataUserAdapter(this, listFromDB);

        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
    }

}
