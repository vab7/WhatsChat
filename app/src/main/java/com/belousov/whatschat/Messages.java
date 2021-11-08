package com.belousov.whatschat;

public class Messages {
    String message, senderId, currentTime;
    long time;

    public Messages() {
    }

    public Messages(String message, String senderId, String currentTime, long time) {
        this.message = message;
        this.senderId = senderId;
        this.currentTime = currentTime;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
