package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.dao.FeedingRecordDAO;
import dk.agrisys.pigfeedingsystem.dao.PigDAO;
import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class WarningService {

    private final PigDAO pigDAO;
    private final FeedingRecordDAO feedingRecordDAO;
    private static final double MINIMUM_KG_PER_3_DAYS = 1.5; // Eksempel grænse

    public WarningService() {
        this.pigDAO = new PigDAO();
        this.feedingRecordDAO = new FeedingRecordDAO();
    }


    public List<Pig> checkWarnings() {
        System.out.println("Service: Tjekker for advarsler...");
        List<Pig> pigs = pigDAO.getAllPigs();
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        Map<String,Double> feedings = feedingRecordDAO.getRecentFeedingsByDate(threeDaysAgo);

        pigs.removeIf(pig ->{
            double totalAmount = feedings.getOrDefault(pig.getTagNumber(),0.0); // 0 default
            return totalAmount < MINIMUM_KG_PER_3_DAYS;
        });


        System.out.println("Service: Advarselstjek færdig. Fundet " + pigs.size() + " grise.");
        return pigs;
    }
}