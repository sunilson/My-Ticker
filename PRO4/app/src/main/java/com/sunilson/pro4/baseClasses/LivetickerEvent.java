package com.sunilson.pro4.baseClasses;

import com.sunilson.pro4.exceptions.LivetickerEventSetException;

/**
 * @author Linus Weiss
 */

public class LivetickerEvent {

    private String type;
    private String content;
    private String authorID;
    private String livetickerID;
    private String thumbnail;
    private Long timestamp;
    private Boolean important;

    public String getLivetickerID() {
        return livetickerID;
    }

    public void setLivetickerID(String livetickerID) {
        this.livetickerID = livetickerID;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) throws LivetickerEventSetException{
        if (content.isEmpty()) {
            throw new LivetickerEventSetException("Content can't be empty!");
        }
        this.content = content;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) throws LivetickerEventSetException {
        if (timestamp == null) {
            throw new LivetickerEventSetException("Timestamp can't be empty!");
        }
        this.timestamp = timestamp;
    }

    public Boolean getImportant() {
        return important;
    }

    public void setImportant(Boolean important) {
        this.important = important;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
