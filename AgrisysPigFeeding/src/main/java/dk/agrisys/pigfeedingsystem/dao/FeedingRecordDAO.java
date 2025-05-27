package dk.agrisys.pigfeedingsystem.dao;

import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DAO class for handling feeding data.
 * Contains methods to retrieve, insert, and update feeding data in the database.
 */
public class FeedingRecordDAO {

    private PigDAO pigData = new PigDAO(); // DAO for handling pig data

    /**
     * Retrieves feeding data since a given date.
     * @param since The date from which feeding data should be retrieved.
     * @return A map with the pig's ID as the key and the feed amount as the value.
     */
    public Map<String, Double> getRecentFeedingsByDate(LocalDateTime since) {
        Map<String, Double> feedingData = new HashMap<>();
        String query = "SELECT FeedingID, FeedingLocation, PigID, [Date], Duration, FeedAmountGrams " +
                "FROM Feeding " +
                "WHERE (? IS NULL OR [Date] >= ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setObject(1, since);
            ps.setObject(2, since);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    feedingData.put(String.valueOf(rs.getLong("PigID")), rs.getDouble("FeedAmountGrams"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving feeding data: " + e.getMessage());
            e.printStackTrace();
        }

        return feedingData;
    }

    /**
     * Retrieves feeding data for a specific pig since a given date.
     * @param pigId The ID of the pig.
     * @param since The date from which feeding data should be retrieved.
     * @return A list of feeding records for the pig.
     */
    public List<FeedingRecord> getRecentFeedingsForPig(String pigId, LocalDateTime since) {
        List<FeedingRecord> feedingRecords = new ArrayList<>();
        String query = "SELECT FeedingID, FeedingLocation, PigID, [Date], Duration, FeedAmountGrams " +
                "FROM Feeding " +
                "WHERE (? IS NULL OR PigID = ?) AND (? IS NULL OR [Date] >= ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setObject(1, pigId);
            ps.setObject(2, pigId);
            ps.setObject(3, since);
            ps.setObject(4, since);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FeedingRecord record = new FeedingRecord(
                            rs.getString("FeedingLocation"),
                            rs.getLong("PigID"),
                            rs.getTimestamp("Date").toLocalDateTime(),
                            rs.getTimestamp("Duration").toLocalDateTime(),
                            rs.getDouble("FeedAmountGrams")
                    );
                    feedingRecords.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving feeding data: " + e.getMessage());
            e.printStackTrace();
        }

        return feedingRecords;
    }

    /**
     * Inserts a list of feeding records into the database.
     * @param records The list of feeding records to insert.
     * @return True if the insertion was successful, otherwise false.
     */
    public boolean batchInsertFeedingRecords(List<FeedingRecord> records) {
        String sql = "INSERT INTO Feeding (FeedingLocation, PigID, Date, Duration, FeedAmountGrams) VALUES (?, ?, ?, ?, ?)";

        // Retrieve existing pig IDs from the database
        Set<Long> existingPigs = pigData.getAllPigIds();

        // Filter pigs that do not already exist in the database
        List<Pig> pigsToSave = records.stream()
                .map(r -> new Pig(String.valueOf(r.getPigId())))
                .filter(p -> !existingPigs.contains(Long.parseLong(p.getTagNumber())))
                .distinct()
                .toList();

        // Insert new pigs first
        if (!pigsToSave.isEmpty()) {
            try {
                pigData.batchSavePigs(pigsToSave);
            } catch (Exception e) {
                System.err.println("PigDAO: Error during batch insertion of pigs: " + e.getMessage());
                return false;
            }
        }

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            int batchSize = 10000;
            int count = 0;

            for (FeedingRecord record : records) {
                ps.setString(1, record.getLocation());
                ps.setLong(2, record.getPigId());
                ps.setTimestamp(3, Timestamp.valueOf(record.getTimestamp()));
                ps.setTimestamp(4, Timestamp.valueOf(record.getDuration()));
                ps.setDouble(5, record.getAmountInGrams());
                ps.addBatch();

                if (++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error during batch insertion of feeding data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserts a single feeding record into the database.
     * @param record The feeding record to insert.
     * @return True if the insertion was successful, otherwise false.
     */
    public boolean saveFeedingRecord(FeedingRecord record) {
        if (!pigData.getPig(record.getPigId())) {
            if (!pigData.savePig(new Pig(String.valueOf(record.getPigId())))) {
                System.err.println("DAO: Error inserting pig for feeding data: " + record.toString());
            }
        }

        String query = "INSERT INTO Feeding (PigID, Date, Duration, FeedAmountGrams) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.getConnection()) {
            if (conn == null) {
                System.err.println("DAO: Database connection is null.");
                return false;
            }
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, record.getPigId());
                stmt.setTimestamp(2, Timestamp.valueOf(record.getTimestamp()));
                stmt.setTimestamp(3, Timestamp.valueOf(record.getDuration()));
                stmt.setDouble(4, record.getAmountInGrams());
                conn.commit();
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
            System.err.println("DAO: Error inserting feeding data: " + e.getMessage());
        }
        return false;
    }

    /**
     * Maps a ResultSet to a FeedingRecord object.
     * @param rs The ResultSet from the database.
     * @return A FeedingRecord object.
     * @throws SQLException If an error occurs during mapping.
     */
    private FeedingRecord mapResultSetToFeedingRecord(ResultSet rs) throws SQLException {
        return new FeedingRecord(
            rs.getString("FeedingLocation"),
            rs.getLong("PigID"),
            rs.getTimestamp("Date").toLocalDateTime(),
            rs.getTimestamp("Duration").toLocalDateTime(),
            rs.getDouble("FeedAmountGrams")
        );
    }
}