package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.dao.FeedingRecordDAO;
import dk.agrisys.pigfeedingsystem.dao.PigDAO;
import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Service class for managing feeding data.
 * Provides methods to retrieve feeding records and pig data from the database.
 */
public class FeedingDataService {

    private final PigDAO pigDAO; // DAO for accessing pig data
    private final FeedingRecordDAO feedingRecordDAO; // DAO for accessing feeding record data

    /**
     * Constructor for FeedingDataService.
     * Initializes the DAOs for pig and feeding record data.
     */
    public FeedingDataService() {
        this.pigDAO = new PigDAO();
        this.feedingRecordDAO = new FeedingRecordDAO();
    }

    /**
     * Retrieves all feeding records from the database.
     * @return A list of FeedingRecord objects, or an empty list if an error occurs.
     */
    public List<FeedingRecord> getFeedingRecords() {
        try {
            List<FeedingRecord> feedingRecords = feedingRecordDAO.getRecentFeedingsForPig(null, null);
            System.out.println("Service: Retrieved " + feedingRecords.size() + " feeding records from the database.");
            return feedingRecords;
        } catch (Exception e) {
            System.err.println("Service: Error retrieving all feeding records: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves all pigs from the database.
     * @return A list of Pig objects, or an empty list if an error occurs.
     */
    public List<Pig> getAllPigs() {
        try {
            List<Pig> pigs = pigDAO.getAllPigs();
            System.out.println("Service: Retrieved " + pigs.size() + " pigs from the database.");
            return pigs;
        } catch (Exception e) {
            System.err.println("Service: Error retrieving all pigs: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves feeding records for a specific pig since a given time.
     * @param pigId The ID of the pig to retrieve feeding records for.
     * @param since The starting time to filter feeding records.
     * @return A list of FeedingRecord objects, or an empty list if an error occurs.
     */
    public List<FeedingRecord> getAllFeedingRecords(String pigId, LocalDateTime since) {
        try {
            List<FeedingRecord> feedingRecords = feedingRecordDAO.getRecentFeedingsForPig(pigId, since);
            System.out.println("Service: Retrieved " + feedingRecords.size() + " feeding records for pig " + pigId);
            return feedingRecords;
        } catch (Exception e) {
            System.err.println("Service: Error retrieving feeding records: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}