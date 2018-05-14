package de.hska.lkit.demo.redis.model;

import java.io.Serializable;

public class Post implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String date;
    private String text;

    public Post() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
