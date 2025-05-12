package dk.agrisys.pigfeedingsystem.model;

import java.time.LocalDateTime;

public class FeedingRecord {
    private String location; // Location of the feeding
    private LocalDateTime duration;
    private double amountInGrams;
    private Long pigId;
    private LocalDateTime timestamp;
    private int id;

    // Constructor
    public FeedingRecord(String location, Long pigId, LocalDateTime timestamp, LocalDateTime duration, double amountInGrams) {
        this.location = location;
        this.pigId = pigId;
        this.timestamp = timestamp;
        this.duration = duration;
        this.amountInGrams = amountInGrams;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public Long getPigId() {
        return pigId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public LocalDateTime getDuration() {
        return duration;
    }

    public double getAmountInGrams() {
        return amountInGrams;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setPigId(Long pigId) {
        this.pigId = pigId;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setAmountInGrams(double amountInGrams) {
        this.amountInGrams = amountInGrams;
    }
}