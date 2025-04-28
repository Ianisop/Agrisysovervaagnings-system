package dk.agrisys.pigfeedingsystem.model;

import java.time.LocalDateTime;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
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

    // Getters...
}