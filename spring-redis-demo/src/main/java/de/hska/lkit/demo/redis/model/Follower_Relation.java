package de.hska.lkit.demo.redis.model;

import java.io.Serializable;
import java.util.*;

public class Follower_Relation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String usernameFollower;
    private List<String> usernamesFollowed;

    public Follower_Relation() {

    }

    public String getUsernameFollower() {
        return usernameFollower;
    }

    public void setUsernameFollower(String usernameFollower) {
        this.usernameFollower = usernameFollower;
    }

    public List<String> getUsernamesFollowed() {
        return usernamesFollowed;
    }

    public void setUsernamesFollowed(List<String> userNamesFollowed) {
        this.usernamesFollowed = usernamesFollowed;
    }
}
