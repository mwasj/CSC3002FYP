package com.example.myapplication.domain_objects;

/**
 * Created by Michal on 07/02/14.
 */
public class FriendRequest {

    private int FriendRequestId;
    private User FromUser;
    private User ToUser;
    private String Message;
    private int Decision;
    private Boolean Read;
    private String SentOnDate;

    public void setFromUser(User fromUser) {
        FromUser = fromUser;
    }

    public void setToUser(User toUser) {
        ToUser = toUser;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public void setDecision(int decision) {
        Decision = decision;
    }

    private String DecidedOnDate;

    public int getFriendRequestId() {
        return FriendRequestId;
    }

    public User getFromUser() {
        return FromUser;
    }

    public User getToUser() {
        return ToUser;
    }

    public String getMessage() {
        return Message;
    }

    public Boolean getRead() {
        return Read;
    }

    public int getDecision() {
        return Decision;
    }

    public String getSentOnDate() {
        return SentOnDate;
    }

    public String getDecidedOnDate() {
        return DecidedOnDate;
    }
}
