package com.example.uberrider.model;

public class DriverInfoModel {
    private String Name,phoneNumber,avatar;
    private double rating;

    public DriverInfoModel() {
    }

    public DriverInfoModel(String firstName,  String phoneNumber, String avatar, double rating) {
        this.Name = firstName;

        this.phoneNumber = phoneNumber;
        this.avatar = avatar;
        this.rating = rating;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
