package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.dao.FeedingRecordDAO;
import dk.agrisys.pigfeedingsystem.dao.PigDAO;
import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class FeedingDataService {

    private final PigDAO pigDAO;
    private final FeedingRecordDAO feedingRecordDAO;

    public FeedingDataService() {
        this.pigDAO = new PigDAO();
        this.feedingRecordDAO = new FeedingRecordDAO();
    }

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