package com.example.pushin_v5.jsonTask;
/***********************************************************************
 This file is used to parse Json string tasks and check if the queries
 is run successfully. According to each data model there will be different
 functions used to parse the Json object.
 ***********************************************************************/

import com.example.pushin_v5.dataModel.DataExam;
import com.example.pushin_v5.dataModel.DataFlashcard;
import com.example.pushin_v5.dataModel.DataMaterials;
import com.example.pushin_v5.dataModel.DataTopic;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParseJson {
    public boolean identification(String result) {
        int aJsonInteger = 2;
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(result);
            aJsonInteger = jObject.getInt("success");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (aJsonInteger == 1) return true;
        else return false;
    }

    public DataExam displaymodule(JSONObject json) {
        DataExam module = new DataExam();
        try {
            int temp_userId = json.getInt("userId");
            int temp_moduleId = json.getInt("moduleId");
            String temp_moduleName = json.getString("moduleName");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date temp_moduleExamDate = (Date) dateFormat.parse(json.getString("moduleExamDate"));
            String temp_moduleExamTime = json.getString("moduleExamTime");
            module = new DataExam(temp_moduleId, temp_userId, temp_moduleName, temp_moduleExamDate, temp_moduleExamTime);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return module;
    }

    public DataTopic displaytopic(JSONObject json) {
        DataTopic topic = new DataTopic();
        try {
            int temp_topicId = json.getInt("topicId");
            int temp_topicOrder = json.getInt("topicOrder");
            int temp_moduleId = json.getInt("moduleId");
            String temp_topicTitle = json.getString("topicTitle");
            String temp_topicDetail = json.getString("topicDetail");
            String temp_timeStamp = json.getString("lastEditTimeStamp");
            int isBookmarkInt = json.getInt("isBookmark");
            boolean isBookmark = false;
            if (isBookmarkInt == 1) isBookmark = true;
            else isBookmark = false;
            topic = new DataTopic(temp_topicId, temp_topicOrder, temp_moduleId, temp_topicTitle, temp_topicDetail, temp_timeStamp, isBookmark);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return topic;
    }

    public DataFlashcard displayflashcard(JSONObject json) {
        DataFlashcard flashcard = new DataFlashcard();
        try {
            int temp_topicId = json.getInt("topicId");
            int temp_flashcardOrder = json.getInt("flashcardOrder");
            int temp_flashcardId = json.getInt("flashcardId");
            String timestamp = json.getString("lastEditFlashcardTimestamp");
            String temp_type = json.getString("contentType");
            int isBookmarkInt = json.getInt("isBookmark");
            boolean isBookmark = false;
            if (isBookmarkInt == 1) isBookmark = true;
            else isBookmark = false;
            flashcard = new DataFlashcard(temp_topicId, temp_flashcardId, temp_flashcardOrder, temp_type, timestamp, isBookmark);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return flashcard;
    }

    public DataMaterials displaymaterials(JSONObject json) {
        DataMaterials module = new DataMaterials();
        try {
            int temp_moduleId = json.getInt("moduleId");
            String temp_moduleName = json.getString("moduleName");
            module = new DataMaterials(temp_moduleId, temp_moduleName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return module;
    }

}