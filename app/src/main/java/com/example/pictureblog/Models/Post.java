package com.example.pictureblog.Models;

import com.google.firebase.database.ServerValue;

public class Post {

    private String title, description, picture, userId, userPhoto, postKey, postLocation, postPlace;
    private Object timeStamp;

    public Post(String title, String description, String picture, String userId, String userPhoto, String postLocation, String postPlace) {
        this.title = title;
        this.description = description;
        this.picture = picture;
        this.userId = userId;
        this.userPhoto = userPhoto;
        this.postLocation = postLocation;
        this.postPlace = postPlace;
        this.timeStamp = ServerValue.TIMESTAMP; //the value of the time stamp is given from the firebase database server
    }

    public Post() {
    }

    public String getPostPlace() {
        return postPlace;
    }

    public String getPostLocation() {
        return postLocation;
    }

    public String getPostKey() {
        return postKey;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPicture() {
        return picture;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public Object getTimeStamp() {
        return timeStamp;
    }

    public void setPostPlace(String postPlace) {
        this.postPlace = postPlace;
    }

    public void setPostLocation(String postLocation) {
        this.postLocation = postLocation;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }
}
