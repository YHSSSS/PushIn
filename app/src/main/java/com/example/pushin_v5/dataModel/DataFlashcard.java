package com.example.pushin_v5.dataModel;

/***********************************************************************
 This file is the data model used for item of flashcard table. The variables
 is created according to the columns of the table. There are setter
 functions used for other java classes.
 ***********************************************************************/
public class DataFlashcard {
    private int topicId;
    private int flashcardId;
    private int flashcardOrder;
    private String contentType;
    private String timestamp;
    private boolean isBookmark;

    public DataFlashcard(int topicId, int flashcardId, int flashcardOrder, String contentType, String timestamp, boolean isBookmarks) {
        this.topicId = topicId;
        this.flashcardId = flashcardId;
        this.flashcardOrder = flashcardOrder;
        this.contentType = contentType;
        this.timestamp = timestamp;
        this.isBookmark = isBookmarks;
    }

    public DataFlashcard() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getFlashcardId() {
        return flashcardId;
    }

    public int getFlashcardOrder() {
        return flashcardOrder;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isBookmark() {
        return isBookmark;
    }

    public void setBookmark(boolean bookmark) {
        isBookmark = bookmark;
    }
}
