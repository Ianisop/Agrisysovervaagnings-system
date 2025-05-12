package dk.agrisys.pigfeedingsystem.dao;

import dk.agrisys.pigfeedingsystem.model.Pig;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class PigDAO {

    private static final List<Pig> pigs = new ArrayList<>();
    private static int nextId = 1;


    public List<Pig> getAllPigs() {
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
            System.err.println("DAO: Error fetching pigs from the database: " + e.getMessage());
            e.printStackTrace();
        }
        return pigs;
    }

    // Check if a pig is valid from db
    public boolean getPig(Long pigId)
    {
        String query = "SELECT 1 FROM Pig WHERE PigID = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, pigId);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // if any row exists, pig exists

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean batchSavePigs(List<Pig> pigs) {
        if (pigs == null || pigs.isEmpty()) return true;

        String sql = "INSERT INTO Pig (PigID, Number, Location, FCR, StartWeight, EndWeight, WeightGain, FeedIntake, TestDays, Duration) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        // Fetch all existing PigIDs from DB
        Set<Long> existingPigs = getAllPigIds();

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            //save just their ids for now using an extra check
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
                    existingPigs.add(pigId);// cache the pig : solves everything
                   // System.out.println(pigId);
                }
            }

            ps.executeBatch();
            conn.commit();
            System.out.println("PigDAO: Batch pig insert completed. New pigs: " + pigs.size());
            return true;

        } catch (SQLException e) {
            System.err.println("PigDAO: Error in batch pig insert: " + e.getMessage());
            return false;
        }
    }

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
        System.out.println("PIGDAO: Fetched " + String.valueOf(pigIds.stream().count()) + "pigIds from DB");
        return pigIds;
    }

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
                int rowsInserted = ps.executeUpdate();
                conn.commit();
                if (rowsInserted > 0) {
                    //System.out.println("DAO: Saved pig " + pig.getTagNumber());
                    return true;
                } else {
                    System.out.println("DAO: Pig insert returned 0 rows.");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.out.println("DAO: Error saving pig " + pig.getTagNumber());
            e.printStackTrace();
            return false;
        }
    }


}