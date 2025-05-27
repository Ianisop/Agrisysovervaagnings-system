package dk.agrisys.pigfeedingsystem.model;

import java.time.LocalDateTime;

/**
 * Model class representing a feeding record.
 * Contains information about the feeding location, duration, amount, pig ID, timestamp, and a unique ID.
 */
public class FeedingRecord {
    private String location; // Location of the feeding
    private LocalDateTime duration; // Duration of the feeding
    private double amountInGrams; // Amount of feed in grams
    private Long pigId; // ID of the pig that was fed
    private LocalDateTime timestamp; // Timestamp of the feeding
    private int id; // Unique ID for the feeding record

    /**
     * Constructor to create a new feeding record.
     * @param location Location of the feeding.
     * @param pigId ID of the pig that was fed.
     * @param timestamp Timestamp of the feeding.
     * @param duration Duration of the feeding.
     * @param amountInGrams Amount of feed in grams.
     */
    public FeedingRecord(String location, Long pigId, LocalDateTime timestamp, LocalDateTime duration, double amountInGrams) {
        this.location = location;
        this.pigId = pigId;
        this.timestamp = timestamp;
        this.duration = duration;
        this.amountInGrams = amountInGrams;
    }

    // Getters

    /**
     * Retrieves the unique ID of the feeding record.
     * @return ID of the feeding record.
     */
    public int getId() {
        return id;
    }

    /**
     * Retrieves the location of the feeding.
     * @return Location of the feeding.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Retrieves the ID of the pig.
     * @return ID of the pig.
     */
    public Long getPigId() {
        return pigId;
    }

    /**
     * Retrieves the timestamp of the feeding.
     * @return Timestamp of the feeding.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Retrieves the duration of the feeding.
     * @return Duration of the feeding.
     */
    public LocalDateTime getDuration() {
        return duration;
    }

    /**
     * Retrieves the amount of feed in grams.
     * @return Amount of feed in grams.
     */
    public double getAmountInGrams() {
        return amountInGrams;
    }

    // Setters

    /**
     * Sets the unique ID of the feeding record.
     * @param id New ID for the feeding record.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the ID of the pig.
     * @param pigId New ID of the pig.
     */
    public void setPigId(Long pigId) {
        this.pigId = pigId;
    }

    /**
     * Sets the timestamp of the feeding.
     * @param timestamp New timestamp for the feeding.
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets the location of the feeding.
     * @param location New location of the feeding.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Sets the amount of feed in grams.
     * @param amountInGrams New amount of feed in grams.
     */
    public void setAmountInGrams(double amountInGrams) {
        this.amountInGrams = amountInGrams;
    }
}