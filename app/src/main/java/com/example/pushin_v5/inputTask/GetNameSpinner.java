package com.example.pushin_v5.inputTask;
/***********************************************************************
 This file is used to set the item name of spinners which will be used in
 the project.
 ***********************************************************************/

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.pushin_v5.jsonTask.HttpConnection;
import com.example.pushin_v5.jsonTask.ParseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GetNameSpinner {
    private HttpConnection httpConnection;
    private String jsonstring;
    private ParseJson pj = new ParseJson();
    private Context context;

    public GetNameSpinner(Context context) {
        this.context = context;
    }

    public List<Integer> module(int userId, Spinner spinner) {
        //set options with the name of the exam
        List<String> moduleName = new ArrayList<String>();
        List<Integer> moduleIdList = new ArrayList<Integer>();
        moduleName.add("PICK AN EXAM");
        moduleIdList.add(0);
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/moduleNameList.php?id=" + userId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null) {
                if (pj.identification(jsonstring)) {
                    JSONArray module = new JSONObject(jsonstring).getJSONArray("module");
                    for (int i = 0; i < module.length(); i++) {
                        //parse json object into data model and insert into array list
                        JSONObject Singlemodule = module.getJSONObject(i);
                        moduleName.add(Singlemodule.getString("moduleName"));
                        moduleIdList.add(Singlemodule.getInt("moduleId"));
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, moduleName);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        return moduleIdList;
    }

    public List<Integer> topic(int moduleId, Spinner spinner) {
        //set options with the name of the topic
        List<String> topicTitle = new ArrayList<String>();
        List<Integer> topicIdList = new ArrayList<Integer>();
        topicTitle.add("PICK A TOPIC");
        topicIdList.add(0);
        String tempUrl = "https://zeno.computing.dundee.ac.uk/2019-projects/jiaxinhuang/topicNameList.php?id=" + moduleId;
        try {
            httpConnection = new HttpConnection();
            jsonstring = httpConnection.execute(tempUrl).get();
            if (jsonstring != null) {
                if (pj.identification(jsonstring)) {
                    JSONArray topic = new JSONObject(jsonstring).getJSONArray("topic");
                    for (int i = 0; i < topic.length(); i++) {
                        //parse data and insert into array list
                        JSONObject Singlemodule = topic.getJSONObject(i);
                        topicTitle.add(Singlemodule.getString("topicTitle"));
                        topicIdList.add(Singlemodule.getInt("topicId"));
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, topicTitle);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        return topicIdList;
    }

    public List<Integer> flashcardType(Spinner spinner) {
        //set the type of the flashcard: text and image
        List<String> contentType = new ArrayList<>();
        List<Integer> typeId = new ArrayList<>();
        contentType.add("PICK THE TYPE OF THE FLASHCARD");
        contentType.add("TEXT");
        contentType.add("IMAGE");
        typeId.add(0);
        typeId.add(1);
        typeId.add(2);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, contentType);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        return typeId;
    }

    public List<String> level(Spinner spinner) {
        //set the level of users
        List<String> categories = new ArrayList<String>();
        List<String> level = new ArrayList<>();
        categories.add("Pick a level");
        categories.add("AC 1st year");
        categories.add("AC 2nd year");
        categories.add("AC 3rd year");
        categories.add("AC 4th year");
        categories.add("AC MSc");

        level.add(null);
        level.add("B1");
        level.add("B2");
        level.add("B3");
        level.add("B4");
        level.add("MS");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        return level;
    }
}
