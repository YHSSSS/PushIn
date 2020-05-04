package com.example.pushin_v5.dataModel;

/***********************************************************************
 This file is the data model used for item of topic table. The variables
 is created according to the columns of the table. There are setter
 functions used for other java classes.
 ***********************************************************************/
public class DataTopic {
    private int topicId;
    private int topicOrder;
    private int moduleId;
    private String topicTitle;
    private String topicDetail;
    private String topicTimeStamp;
    private boolean isBookmark;

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getTopicOrder() {
        return topicOrder;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public String getTopicDetail() {
        return topicDetail;
    }

    public String getTopicTimeStamp() {
        return topicTimeStamp;
    }

    public boolean isBookmark() {
        return isBookmark;
    }

    public void setBookmark(boolean bookmark) {
        isBookmark = bookmark;
    }

    public DataTopic() {
    }

    public DataTopic(int topicId, int topicOrder, int moduleId, String topicTitle,
                     String topicDetail, String topicTimeStamp, boolean isBookmark) {
        this.topicId = topicId;
        this.topicOrder = topicOrder;
        this.moduleId = moduleId;
        this.topicTitle = topicTitle;
        this.topicDetail = topicDetail;
        this.topicTimeStamp = topicTimeStamp;
        this.isBookmark = isBookmark;
    }
}