package com.example.uberrider.model;

public class Sender {
    public Notification notification;
    public String to;

    public Sender(Notification notification, String to) {
        this.notification = notification;
        this.to = to;
    }

    public Notification getData() {
        return notification;
    }

    public void setData(Notification notification) {
        this.notification = notification;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Sender() {
    }
}
