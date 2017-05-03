package com.sunilson.pro4.baseClasses;

/**
 * @author Linus Weiss
 */

public class Comment {
    private String text, authorID;
    private Long timestamp;

    public Comment(String text, String authorID) {
        this.text = text;
        this.authorID = authorID;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }
}
