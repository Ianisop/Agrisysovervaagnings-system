package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.dao.FeedingRecordDAO;
import dk.agrisys.pigfeedingsystem.dao.PigDAO;
import dk.agrisys.pigfeedingsystem.model.Pig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service class for managing warnings related to pigs' feeding behavior.
 * Checks if pigs have consumed less than a specified amount of feed over the last three days.
 */
public class WarningService {

    private final PigDAO pigDAO; // DAO for accessing pig data
    private final FeedingRecordDAO feedingRecordDAO; // DAO for accessing feeding record data
    private static final double MINIMUM_KG_PER_3_DAYS = 1.5; // Minimum feed consumption threshold in kg over 3 days

    /**
     * Constructor for WarningService.
     * Initializes the DAOs for pig and feeding record data.
     */
    public WarningService() {
        this.pigDAO = new PigDAO();
        this.feedingRecordDAO = new FeedingRecordDAO();
    }

    /**
     * Checks for pigs that have consumed less than the minimum feed threshold over the last three days.
     * @return A list of pigs that meet the warning criteria.
     */
    public List<Pig> checkWarnings() {
        System.out.println("Service: Checking for warnings...");

        // Retrieve all pigs from the database
        List<Pig> pigs = pigDAO.getAllPigs();

        // Calculate the date and time three days ago
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        // Retrieve feeding data for the last three days
        Map<String, Double> feedings = feedingRecordDAO.getRecentFeedingsByDate(threeDaysAgo);

        // Remove pigs that have consumed more than the minimum threshold
        pigs.removeIf(pig -> {
            double totalAmount = feedings.getOrDefault(pig.getTagNumber(), 0.0); // Default to 0 if no data exists
            return totalAmount >= MINIMUM_KG_PER_3_DAYS;
        });

        System.out.println("Service: Warning check complete. Found " + pigs.size() + " pigs with warnings.");
        return pigs;
    }
}