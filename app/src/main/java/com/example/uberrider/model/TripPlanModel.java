package com.example.uberrider.model;

import com.example.uberrider.model.RiderModel;
import com.example.uberrider.model.User;

public class TripPlanModel {
    private String rider,driver;
    private User driverInfoModel;
    private RiderModel riderModel;
    String originString,origin;
    private String destination,destinationString;
    private String distancePickup,distancedestination;
    private String durationPickup,durationDestination;
    private double currentLat,currentLng;
    private boolean isDone,isCancel;

    public String getOriginString() {
        return originString;
    }

    public void setOriginString(String originString) {
        this.originString = originString;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public TripPlanModel() {
    }

    public TripPlanModel(String rider, String driver, User driverInfoModel, RiderModel riderModel, String destination, String destinationString, String distancePickup, String distancedestination, String durationPickup, String durationDestination, double currentLat, double currentLng, boolean isDone, boolean isCancel) {
        this.rider = rider;
        this.driver = driver;
        this.driverInfoModel = driverInfoModel;
        this.riderModel = riderModel;
        this.destination = destination;
        this.destinationString = destinationString;
        this.distancePickup = distancePickup;
        this.distancedestination = distancedestination;
        this.durationPickup = durationPickup;
        this.durationDestination = durationDestination;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
        this.isDone = isDone;
        this.isCancel = isCancel;
    }

    public String getRider() {
        return rider;
    }

    public void setRider(String rider) {
        this.rider = rider;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public User getDriverInfoModel() {
        return driverInfoModel;
    }

    public void setDriverInfoModel(User driverInfoModel) {
        this.driverInfoModel = driverInfoModel;
    }

    public RiderModel getRiderModel() {
        return riderModel;
    }

    public void setRiderModel(RiderModel riderModel) {
        this.riderModel = riderModel;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestinationString() {
        return destinationString;
    }

    public void setDestinationString(String destinationString) {
        this.destinationString = destinationString;
    }

    public String getDistancePickup() {
        return distancePickup;
    }

    public void setDistancePickup(String distancePickup) {
        this.distancePickup = distancePickup;
    }

    public String getDistancedestination() {
        return distancedestination;
    }

    public void setDistancedestination(String distancedestination) {
        this.distancedestination = distancedestination;
    }

    public String getDurationPickup() {
        return durationPickup;
    }

    public void setDurationPickup(String durationPickup) {
        this.durationPickup = durationPickup;
    }

    public String getDurationDestination() {
        return durationDestination;
    }

    public void setDurationDestination(String durationDestination) {
        this.durationDestination = durationDestination;
    }

    public double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(double currentLat) {
        this.currentLat = currentLat;
    }

    public double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(double currentLng) {
        this.currentLng = currentLng;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }
}
