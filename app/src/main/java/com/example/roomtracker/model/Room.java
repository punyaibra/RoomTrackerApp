package com.example.roomtracker.model;

public class Room {
    private int id;
    private String name;
    private String building;
    private String floor;
    private int capacity;
    private String size;
    private String facilities;
    private String description;
    private String imageUri;
    private String status; // ACTIVE or INACTIVE

    public Room() {
        this.status = "ACTIVE";
    }

    public Room(int id, String name, String building, String floor, int capacity, String size, String facilities,
            String description, String imageUri) {
        this.id = id;
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.capacity = capacity;
        this.size = size;
        this.facilities = facilities;
        this.description = description;
        this.imageUri = imageUri;
        this.status = "ACTIVE"; // Default for existing constructors
    }

    public Room(int id, String name, String building, String floor, int capacity, String size, String facilities,
            String description, String imageUri, String status) {
        this.id = id;
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.capacity = capacity;
        this.size = size;
        this.facilities = facilities;
        this.description = description;
        this.imageUri = imageUri;
        this.status = status;
    }

    public Room(String name, String building, String floor, int capacity, String size, String facilities,
            String description, String imageUri) {
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.capacity = capacity;
        this.size = size;
        this.facilities = facilities;
        this.description = description;
        this.imageUri = imageUri;
        this.status = "ACTIVE";
    }

    public Room(String name, String building, String floor, int capacity, String size, String facilities,
            String description, String imageUri, String status) {
        this.name = name;
        this.building = building;
        this.floor = floor;
        this.capacity = capacity;
        this.size = size;
        this.facilities = facilities;
        this.description = description;
        this.imageUri = imageUri;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper for Location display
    public String getLocation() {
        return (building != null ? building : "") + (floor != null && !floor.isEmpty() ? ", Floor " + floor : "");
    }
}
