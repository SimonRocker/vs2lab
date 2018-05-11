package de.hska.lkit.demo.redis.model;

import java.io.Serializable;

public class Token implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String ip;
    private String toDate;

    public Token() {

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

    public void setUserId(String userID) {
        this.userId = userID;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.toDate = ip;
    }
}
