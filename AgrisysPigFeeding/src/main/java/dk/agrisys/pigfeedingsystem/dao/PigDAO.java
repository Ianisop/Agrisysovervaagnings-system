package dk.agrisys.pigfeedingsystem.dao;

import dk.agrisys.pigfeedingsystem.model.Pig;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO class for handling pig data.
 * Contains methods to retrieve, save, and update pig data in the database.
 */
public class PigDAO {

    private static final List<Pig> pigs = new ArrayList<>(); // List to hold pigs in memory
    private static int nextId = 1; // Next ID for pigs (used only if no database is available)

    /**
     * Retrieves all pigs from the database.
     * @return A list of pigs.
     */
    public List<Pig> getAllPigs() {
        long startTime = System.currentTimeMillis();
        List<Pig> pigs = new ArrayList<>();
        String query = "SELECT PigID, Number, Location, FCR, StartWeight, EndWeight, WeightGain, FeedIntake, TestDays, Duration FROM Pig";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pig pig = new Pig(
                        rs.getInt("Location"),
                        String.valueOf(rs.getLong("PigID")),
                        rs.getFloat("FCR"),
                        rs.getFloat("StartWeight"),
                        rs.getFloat("FeedIntake"),
                        rs.getFloat("WeightGain"),
                        rs.getFloat("EndWeight"),
                        rs.getInt("TestDays")
                );
                pigs.add(pig);
            }
            System.out.println("DAO: Retrieved " + pigs.size() + " pigs from the database.");
        } catch (SQLException e) {
            System.err.println("DAO: Error retrieving pigs from the database: " + e.getMessage());
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        return pigs;
    }

    /**
     * Checks if a pig with a specific ID exists in the database.
     * @param pigId The ID of the pig.
     * @return True if the pig exists, otherwise false.
     */
    public boolean getPig(Long pigId) {
        String query = "SELECT 1 FROM Pig WHERE PigID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, pigId);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // If a row exists, the pig exists

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves a list of pigs to the database in batches.
     * @param pigs The list of pigs to save.
     * @return True if the insertion was successful, otherwise false.
     */
    public boolean batchSavePigs(List<Pig> pigs) {
        if (pigs == null || pigs.isEmpty()) return true;

        String sql = "INSERT INTO Pig (PigID, Number, Location, FCR, StartWeight, EndWeight, WeightGain, FeedIntake, TestDays, Duration) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Set<Long> existingPigs = getAllPigIds(); // Retrieve existing pig IDs from the database

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Pig pig : pigs) {
                long pigId = Long.parseLong(pig.getTagNumber());

                if (!existingPigs.contains(pigId)) {
                    ps.setLong(1, pigId);
                    ps.setInt(2, pig.getNumber());
                    ps.setInt(3, pig.getLocation());
                    ps.setFloat(4, pig.getFCR());
                    ps.setFloat(5, pig.getStartWeight());
                    ps.setFloat(6, pig.getEndWeight());
                    ps.setFloat(7, pig.getWeightGain());
                    ps.setFloat(8, pig.getFeedIntake());
                    ps.setInt(9, pig.getTestDays());
                    ps.setFloat(10, pig.getDuration());
                    ps.addBatch();
                    existingPigs.add(pigId); // Add the pig to the cache
                }
            }

            ps.executeBatch();
            conn.commit();
            System.out.println("PigDAO: Batch insertion of pigs completed. New pigs: " + pigs.size());
            return true;

        } catch (SQLException e) {
            System.err.println("PigDAO: Error during batch insertion of pigs: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all pig IDs from the database.
     * @return A set of pig IDs.
     */
    public Set<Long> getAllPigIds() {
        Set<Long> pigIds = new HashSet<>();
        String sql = "SELECT PigID FROM Pig";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                pigIds.add(rs.getLong("PigID"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("PigDAO: Retrieved " + pigIds.size() + " pig IDs from the database.");
        return pigIds;
    }

    /**
     * Saves a single pig to the database.
     * @param pig The pig to save.
     * @return True if the insertion was successful, otherwise false.
     */
    public boolean savePig(Pig pig) {
        String query = "INSERT INTO Pig(PigID, Number, Location, FCR, StartWeight, EndWeight, WeightGain, FeedIntake, TestDays, Duration) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if (getPig(Long.parseLong(pig.getTagNumber()))) {
            System.out.println("DAO: Pig with ID " + pig.getTagNumber() + " already exists.");
            return false;
        }

        try (Connection conn = DatabaseConnector.getConnection()) {
            if (conn == null) {
                System.err.println("DAO: Database connection is null.");
                return false;
            }
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setLong(1, Long.parseLong(pig.getTagNumber()));
                ps.setInt(2, pig.getNumber());
                ps.setInt(3, pig.getLocation());
                ps.setFloat(4, pig.getFCR());
                ps.setFloat(5, pig.getStartWeight());
                ps.setFloat(6, pig.getEndWeight());
                ps.setFloat(7, pig.getWeightGain());
                ps.setFloat(8, pig.getFeedIntake());
                ps.setInt(9, pig.getTestDays());
                ps.setFloat(10, pig.getDuration());
                int rowsInserted = ps.executeUpdate();
                conn.commit();
                return rowsInserted > 0;

            }

        } catch (SQLException e) {
            System.out.println("DAO: Error inserting pig " + pig.getTagNumber());
            e.printStackTrace();
            return false;
        }
    }
}