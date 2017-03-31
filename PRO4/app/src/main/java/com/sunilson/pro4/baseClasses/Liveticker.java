package com.sunilson.pro4.baseClasses;

/**
 * @author Linus Weiss
 */

import com.sunilson.pro4.exceptions.LivetickerSetException;

/**
 *
 */
public class Liveticker {

    private String livetickerID;
    private String title;
    private String description;
    private String author;
    private String state;
    private String status;
    private String authorID;
    private int commentCount;
    private Long startDate;

    public Liveticker() {

    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) throws LivetickerSetException {
        if (privacy.isEmpty()) {
            throw new LivetickerSetException("Privacy not valid");
        }
        this.privacy = privacy;
    }

    private String privacy;

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) throws LivetickerSetException {
        if (authorID.isEmpty()) {
            throw new LivetickerSetException("Author ID not valid");
        }
        this.authorID = authorID;
    }

    public String getLivetickerID() {
        return livetickerID;
    }

    public void setLivetickerID(String livetickerID) throws LivetickerSetException {
        if (livetickerID.isEmpty()) {
            throw new LivetickerSetException("Liveticker ID not valid");
        }
        this.livetickerID = livetickerID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) throws LivetickerSetException {
        if (title.isEmpty()) {
            throw new LivetickerSetException("Title is empty!");
        }

        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) throws LivetickerSetException {
        if (description.isEmpty()) {
            throw new LivetickerSetException("Description is empty");
        }
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) throws LivetickerSetException  {
        if (author.isEmpty()) {
            throw new LivetickerSetException("Author is not defined");
        }
        this.author = author;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) throws LivetickerSetException {
        if (state.isEmpty()) {
            throw new LivetickerSetException("Status is not valid");
        }
        this.state = state;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) throws LivetickerSetException {
        if (commentCount < 0) {
            throw new LivetickerSetException("Comment Count too low");
        }
        this.commentCount = commentCount;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) throws LivetickerSetException {
        if (startDate < 0) {
            throw new LivetickerSetException("Start Date not valid");
        }
        this.startDate = startDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) throws LivetickerSetException {
        if (status.isEmpty()) {
            throw new LivetickerSetException("Status is not valid");
        }
        this.status = status;
    }
}
