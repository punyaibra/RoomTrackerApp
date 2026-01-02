package com.example.roomtracker.model;

public class Booking {
    private int id;
    private int roomId;
    private int userId;
    private String date; // Format: YYYY-MM-DD
    private String startTime; // Format: HH:MM
    private String endTime; // Format: HH:MM
    private String status; // "PENDING", "APPROVED", "REJECTED", "COMPLETED"
    private String reason; // New field

    // Helper fields for display (joins)
    private String roomName;
    private String userName;
    private String roomImageUri;

    public Booking() {
    }

    public Booking(int id, int roomId, int userId, String date, String startTime, String endTime, String status,
            String reason) {
        this.id = id;
        this.roomId = roomId;
        this.userId = userId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.reason = reason;
    }

    public Booking(int roomId, int userId, String date, String startTime, String endTime, String status,
            String reason) {
        this.roomId = roomId;
        this.userId = userId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.reason = reason;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoomImageUri() {
        return roomImageUri;
    }

    public void setRoomImageUri(String roomImageUri) {
        this.roomImageUri = roomImageUri;
    }
}
