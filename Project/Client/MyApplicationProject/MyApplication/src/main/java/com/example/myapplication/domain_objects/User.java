package com.example.myapplication.domain_objects;

import java.util.ArrayList;

/**
 * Created by Michal on 05/11/13.
 */
public class User {

    private int UserId;

    private String UserName;

    private String EmailAddress;

    private String FirstName;

    private String LastName;

    private Integer Gender;

    private String DateOfBirth;

    private String GCMRegistrationID;

    private ArrayList<User> Friends;

    private int Status;

    private String LastLogon;

    public double getAverageRating() {
        return AverageRating;
    }

    private String MemberSince;

    private String PhoneNumber;

    public int getUnreadMessagesCount() {
        return UnreadMessagesCount;
    }

    private double AverageRating;

    private int VotesCount;

    public int getVotesCount() {
        return VotesCount;
    }

    private ArrayList<Rating> Ratings;

    private int UnreadMessagesCount;

    private Boolean JourneysVisible;

    public Boolean getJourneysVisible() {
        return JourneysVisible;
    }

    public ArrayList<Rating> getRatings() {
        return Ratings;
    }

    public String getLastLogon() {
        return LastLogon;
    }

    public String getMemberSince() {
        return MemberSince;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public String getGCMRegistrationID() {
        return GCMRegistrationID;
    }

    public String getUserName() {
        return UserName;
    }

    public String getEmailAddress() {
        return EmailAddress;
    }

    public int getUserId() {
        return UserId;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public Integer getGender() {
        return Gender;
    }

    public String getDateOfBirth() {
        return DateOfBirth;
    }

    public ArrayList<User> getFriends() {
        return Friends;
    }

    public int getStatus() {
        return Status;
    }

    private PrivacySettings PrivacySettings;

    public PrivacySettings getPrivacySettings() {
        return PrivacySettings;
    }

    /*
     * This overload is required by GSON.
     */
    public User()
    {
    }


    /*
     * This overload is called by the RegistrationActivity to create a basic user object.
     */
    public User(String userName, String email, String gcmRegistrationId)
    {
        this.UserName = userName;
        this.EmailAddress = email;
        this.GCMRegistrationID = gcmRegistrationId;
    }
}
