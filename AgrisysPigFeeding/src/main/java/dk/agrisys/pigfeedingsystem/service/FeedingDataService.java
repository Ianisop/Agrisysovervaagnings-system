package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.dao.FeedingRecordDAO;
import dk.agrisys.pigfeedingsystem.dao.PigDAO;
import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;

import java.util.List;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class FeedingDataService {

    private final PigDAO pigDAO;
    private final FeedingRecordDAO feedingRecordDAO;

    public FeedingDataService() {
        this.pigDAO = new PigDAO();
        this.feedingRecordDAO = new FeedingRecordDAO();
    }

    public List<Pig> getAllPigs() {
        return pigDAO.getAllPigs();
    }

    public List<FeedingRecord> getAllFeedingRecords() {
        return feedingRecordDAO.getAllFeedingRecords();
    }

    public List<FeedingRecord> getFeedingsForPig(Pig pig) {
        if (pig == null) return List.of(); // Returner tom liste hvis gris er null
        return feedingRecordDAO.getFeedingsForPig(pig.getId());
    }

    // Tilføj metoder til at hente data baseret på datoer, beregne gennemsnit etc.
}