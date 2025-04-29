package dk.agrisys.pigfeedingsystem.dao;

import dk.agrisys.pigfeedingsystem.model.FeedingRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedingRecordDAO {

    public List<FeedingRecord> getAllFeedingRecords() {
        List<FeedingRecord> records = new ArrayList<>();
        String query = "SELECT * FROM Feeding";

        try (Connection conn = DatabaseConnector.getConnection()) {
            if (conn == null) {
                System.err.println("DAO: Database connection is null.");
                return records;
            }
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    records.add(mapResultSetToFeedingRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO: Error fetching all feeding records: " + e.getMessage());
        }
        return records;
    }

    public List<FeedingRecord> getFeedingsForPig(int pigId) {
        List<FeedingRecord> records = new ArrayList<>();
        String query = "SELECT * FROM Feeding WHERE PigID = ?";

        try (Connection conn = DatabaseConnector.getConnection()) {
            if (conn == null) {
                System.err.println("DAO: Database connection is null.");
                return records;
            }
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, pigId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        records.add(mapResultSetToFeedingRecord(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO: Error fetching feeding records for PigID " + pigId + ": " + e.getMessage());
        }
        return records;
    }

    public List<FeedingRecord> getRecentFeedingsForPig(int pigId, LocalDateTime since) {
        List<FeedingRecord> records = new ArrayList<>();
        String query = "SELECT * FROM Feeding WHERE PigID = ? AND Date > ?";

        try (Connection conn = DatabaseConnector.getConnection()) {
            if (conn == null) {
                System.err.println("DAO: Database connection is null.");
                return records;
            }
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, pigId);
                stmt.setTimestamp(2, Timestamp.valueOf(since));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        records.add(mapResultSetToFeedingRecord(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO: Error fetching recent feeding records for PigID " + pigId + ": " + e.getMessage());
        }
        return records;
    }

    public boolean saveFeedingRecord(FeedingRecord record) {
        String query = "INSERT INTO Feeding (PigID, Date, FeedAmountGrams) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnector.getConnection()) {
            if (conn == null) {
                System.err.println("DAO: Database connection is null.");
                return false;
            }
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, record.getPigId());
                stmt.setTimestamp(2, Timestamp.valueOf(record.getTimestamp()));
                stmt.setDouble(3, record.getAmountKg());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            record.setId(generatedKeys.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO: Error saving feeding record: " + e.getMessage());
        }
        return false;
    }

    private FeedingRecord mapResultSetToFeedingRecord(ResultSet rs) throws SQLException {
        return new FeedingRecord(
                rs.getInt("ID"),
                rs.getInt("PigID"),
                rs.getTimestamp("Date").toLocalDateTime(),
                rs.getDouble("FeedAmountGrams")
        );
    }
}