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
    private String state;
    private String status;
    private String authorID;
    private String userName;
    private String profilePicture;
    private int commentCount, likeCount, viewerCount;
    private Long stateTimestamp;

    public Liveticker() {
        commentCount = 0;
        likeCount = 0;
        viewerCount = 0;
    }

    public int getViewerCount() {
        return viewerCount;
    }

    public void setViewerCount(int viewerCount) {
        this.viewerCount = viewerCount;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) throws LivetickerSetException {
        if (privacy == null || privacy.isEmpty()) {
            throw new LivetickerSetException("Privacy not valid");
        }
        this.privacy = privacy;
    }

    private String privacy;

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) throws LivetickerSetException {
        if (authorID == null || authorID.isEmpty()) {
            throw new LivetickerSetException("Author ID not valid");
        }
        this.authorID = authorID;
    }

    public String getLivetickerID() {
        return this.livetickerID;
    }

    public void setLivetickerID(String livetickerID) throws LivetickerSetException {
        if (livetickerID == null || livetickerID.isEmpty()) {
            throw new LivetickerSetException("Liveticker ID not valid");
        }
        this.livetickerID = livetickerID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) throws LivetickerSetException {
        if (title == null || title.isEmpty()) {
            throw new LivetickerSetException("Title is empty!");
        }

        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Long getStateTimestamp() {
        return stateTimestamp;
    }

    public void setStateTimestamp(Long stateTimestamp) throws LivetickerSetException {
        if (stateTimestamp < 0) {
            throw new LivetickerSetException("Start Date not valid");
        }
        this.stateTimestamp = stateTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
