package dk.agrisys.pigfeedingsystem.dao;

import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;
import dk.agrisys.pigfeedingsystem.service.FeedingDataService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FeedingRecordDAO {

    private PigDAO pigData = new PigDAO();


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
            System.err.println("Error fetching feeding records: " + e.getMessage());
            e.printStackTrace();
        }

        return feedingRecords;
    }


    private List<FeedingRecord> fetchFeedingRecords() {
        FeedingDataService service = new FeedingDataService();
        List<FeedingRecord> feedingRecords = service.getAllFeedingRecords(null, LocalDateTime.of(1, 1, 1, 0, 0));
        System.out.println("DEBUG: Fetched " + feedingRecords.size() + " feeding records.");
        for (FeedingRecord record : feedingRecords) {
            System.out.println("DEBUG: FeedingRecord - PigID: " + record.getPigId() + ", Amount: " + record.getAmountInGrams());
        }
        return service.getAllFeedingRecords(null, LocalDateTime.of(1, 1, 1, 0, 0));
    }

    public boolean batchInsertFeedingRecords(List<FeedingRecord> records) {
        String sql = "INSERT INTO Feeding (FeedingLocation, PigID, Date, Duration, FeedAmountGrams) VALUES (CAST(? AS INT), ?, ?, ?, ?)";

        // Fetch all existing PigIDs from DB
        Set<Long> existingPigs = pigData.getAllPigIds();




        // Filter pigs that don't already exist in DB
        List<Pig> pigsToSave = records.stream()
                .map(r -> new Pig(String.valueOf(r.getPigId())))
                .filter(p -> !existingPigs.contains(Long.parseLong(p.getTagNumber())))
                .distinct()
                .toList();

        // Insert new pigs first
        if (!pigsToSave.isEmpty()) {
            try {
                pigData.batchSavePigs(pigsToSave); // Must succeed before feeding insert
            } catch (Exception e) {
                System.err.println("PigDAO: Error in batch pig insert: " + e.getMessage());
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

            ps.executeBatch(); // Insert any remaining records
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public boolean saveFeedingRecord(FeedingRecord record) {
        System.out.println("GETTING PIG WITH VALUE " + record.getPigId());
        if(!pigData.getPig(record.getPigId()))
        {
            if(!pigData.savePig(new Pig(String.valueOf(record.getPigId()))))System.err.println("DAO: error saving pig in feeding record " + record.toString());



            // save pig first if it doesnt exist
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
                stmt.setTimestamp(3,Timestamp.valueOf(record.getDuration()));
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
            System.err.println("DAO: Error saving feeding record: " + e.getMessage());
        }
        return false;
    }


    private FeedingRecord mapResultSetToFeedingRecord(ResultSet rs) throws SQLException {
        return new FeedingRecord(
            String.valueOf(rs.getInt("FeedingLocation")), // Convert int to String for location
            rs.getLong("PigID"),
            rs.getTimestamp("Date").toLocalDateTime(),
            rs.getTimestamp("Duration").toLocalDateTime(),
            rs.getDouble("FeedAmountGrams")
        );
    }
}