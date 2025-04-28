package dk.agrisys.pigfeedingsystem.dao;

import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class FeedingRecordDAO {

    private static final List<FeedingRecord> records = new ArrayList<>();
    private static int nextId = 1;

    static {
        // Tilføj et par test-fodringer (kræver PigDAO er initialiseret først - lidt usikkert design her)
        // Normalt ville Pig ID komme fra databasen
        PigDAO tempPigDao = new PigDAO(); // Kun for at få fat i mock grise ID'er
        List<Pig> tempPigs = tempPigDao.getAllPigs();
        if (!tempPigs.isEmpty()) {
            records.add(new FeedingRecord(nextId++, tempPigs.get(0).getId(), LocalDateTime.now().minusDays(4), 2.1));
            records.add(new FeedingRecord(nextId++, tempPigs.get(1).getId(), LocalDateTime.now().minusDays(3), 2.5));
            records.add(new FeedingRecord(nextId++, tempPigs.get(0).getId(), LocalDateTime.now().minusDays(2), 2.2));
            records.add(new FeedingRecord(nextId++, tempPigs.get(1).getId(), LocalDateTime.now().minusDays(2), 0.5)); // Advarsel for gris B007
            records.add(new FeedingRecord(nextId++, tempPigs.get(0).getId(), LocalDateTime.now().minusDays(1), 2.3));
            records.add(new FeedingRecord(nextId++, tempPigs.get(1).getId(), LocalDateTime.now().minusDays(1), 0.4)); // Advarsel for gris B007
            records.add(new FeedingRecord(nextId++, tempPigs.get(2).getId(), LocalDateTime.now().minusDays(1), 3.0));
            records.add(new FeedingRecord(nextId++, tempPigs.get(1).getId(), LocalDateTime.now().minusHours(5), 0.6)); // Advarsel for gris B007
        }
    }

    public List<FeedingRecord> getAllFeedingRecords() {
        System.out.println("DAO: Henter alle fodringer (MOCK)");
        return new ArrayList<>(records);
    }

    public List<FeedingRecord> getFeedingsForPig(int pigId) {
        System.out.println("DAO: Henter fodringer for gris ID " + pigId + " (MOCK)");
        return records.stream()
                .filter(r -> r.getPigId() == pigId)
                .collect(Collectors.toList());
    }

    public List<FeedingRecord> getRecentFeedingsForPig(int pigId, LocalDateTime since) {
        System.out.println("DAO: Henter fodringer for gris ID " + pigId + " siden " + since + " (MOCK)");
        return records.stream()
                .filter(r -> r.getPigId() == pigId && r.getTimestamp().isAfter(since))
                .collect(Collectors.toList());
    }

    public void saveFeedingRecord(FeedingRecord record) {
        System.out.println("DAO: Gemmer fodring for gris ID " + record.getPigId() + " (MOCK - tilføjer ikke rigtigt)");
        // I en rigtig DAO: Implementer SQL INSERT
    }
}