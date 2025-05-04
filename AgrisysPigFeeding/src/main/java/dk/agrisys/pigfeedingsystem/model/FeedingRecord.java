package dk.agrisys.pigfeedingsystem.model;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.TemporalAccessor;

public class FeedingRecord {
    private final int location;
    private final LocalDateTime duration;
    private final double amountInGrams;
    private Long pigId;
    private LocalDateTime timestamp;

    public FeedingRecord(int location, Long pigId, LocalDateTime timestamp, LocalDateTime duration, double amountInGrams) {
        this.location = location;
        this.pigId = pigId;
        this.timestamp = timestamp;
        this.duration = duration;
        this.amountInGrams = amountInGrams;

    }

    public int getLocation() { return location; }
    public Long getPigId() { return pigId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getAmountInGrams() { return amountInGrams; }


    public void setPigId(Long pigId) {

    }

    public void setDate(String date) {
    }

    public void setAmountKg(Double amount) {
    }

    public void setId(int i) {
    }

    public LocalDateTime getDuration() {
        return duration;
    }
}