package de.hska.lkit.demo.redis.model;

import java.io.Serializable;

public class Token implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
