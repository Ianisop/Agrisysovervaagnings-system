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
        System.out.println("DAO: Henter alle grise (MOCK)");
        return new ArrayList<>(pigs); // Returner kopi for at undgå ekstern ændring
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
                "VALUES (?, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)";

        // Remove pigs that already exist
        Set<Long> existingPigs = getAllPigIds();
        List<Pig> newPigs = pigs.stream()
                .filter(p -> !existingPigs.contains(Long.parseLong(p.getTagNumber())))
                .toList();

        if (newPigs.isEmpty()) return true;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Pig pig : newPigs) {
                ps.setLong(1, Long.parseLong(pig.getTagNumber()));
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
            System.out.println("PigDAO: Batch pig insert completed. New pigs: " + newPigs.size());
            return true;

        } catch (SQLException e) {
            System.err.println("PigDAO: Error in batch pig insert: " + e.getMessage());
            return false;
        }
    }

    public Set<Long> getAllPigIds() {
        Set<Long> pigIds = new HashSet<>();
        String query = "SELECT PigID FROM Pig";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                pigIds.add(rs.getLong("PigID"));
            }
        } catch (SQLException e) {
            System.err.println("PigDAO: Error fetching pig IDs: " + e.getMessage());
        }

        return pigIds;
    }

    public boolean savePig(Pig pig) {
        String query = "INSERT INTO Pig(PigID, Number, Location, FCR, StartWeight, EndWeight, WeightGain, FeedIntake, TestDays, Duration) " +
                "VALUES (?, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)";

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