package com.example.myapplication.DTOs;

import com.example.myapplication.DomainObjects.User;

import java.util.List;

/**
 * Created by Michal on 06/11/13.
 */
public class CarShareDTO {
    public User GetUser() {return Driver;}
    public List<User> GetParticipants() {return Participants;}
    private User Driver;

    private String DepartureCity;
    private String DestinationCity;
    private String Description;
    private String DateOfDeparture;
    private String TimeOfDeparture;
    private double Fee;
    private int AvailableSeats;
    private List<User> Participants;
    private boolean SmokersAllowed;
    private boolean WomenOnly;
    private int VehicleType;

    public CarShareDTO()
    {

    }
}