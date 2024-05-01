package com.example.my_app.models;

public class Rating {
    private String uid;
    private float rating;
    private String comment;

    public Rating() {
    }

    public Rating(String uid, int rating, String comment) {
        this.uid = uid;
        this.rating = rating;
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
