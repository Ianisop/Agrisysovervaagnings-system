package dk.agrisys.pigfeedingsystem.model;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.TemporalAccessor;

public class FeedingRecord {
    private int id;
    private int pigId;
    private LocalDateTime timestamp;
    private double amountKg;

    public FeedingRecord(int id, int pigId, LocalDateTime timestamp, double amountKg) {
        this.id = id;
        this.pigId = pigId;
        this.timestamp = timestamp;
        this.amountKg = amountKg;
    }

    public int getId() { return id; }
    public int getPigId() { return pigId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getAmountKg() { return amountKg; }

    public TemporalAccessor getDate() {
        return timestamp.toLocalDate(); // Extracts and returns the date part
    }

    public void setPigId(Integer pigId) {

    }

    public void setDate(LocalDate date) {
    }

    public void setAmountKg(Double amount) {
    }

    public void setId(int i) {
    }
}