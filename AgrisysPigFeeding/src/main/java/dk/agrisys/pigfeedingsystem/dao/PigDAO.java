package dk.agrisys.pigfeedingsystem.dao;

import dk.agrisys.pigfeedingsystem.model.Pig;
import java.util.ArrayList;
import java.util.List;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class PigDAO {

    private static final List<Pig> pigs = new ArrayList<>();
    private static int nextId = 1;

    static {
        // Tilføj et par test-grise
        pigs.add(new Pig(nextId++, "A001"));
        pigs.add(new Pig(nextId++, "B007"));
        pigs.add(new Pig(nextId++, "C042"));
    }

    public List<Pig> getAllPigs() {
        System.out.println("DAO: Henter alle grise (MOCK)");
        return new ArrayList<>(pigs); // Returner kopi for at undgå ekstern ændring
    }

    public void savePig(Pig pig) {
        System.out.println("DAO: Gemmer gris " + pig.getTagNumber() + " (MOCK - tilføjer ikke rigtigt)");
        // I en rigtig DAO:
        // Connection conn = DatabaseConnector.getConnection();
        // PreparedStatement stmt = conn.prepareStatement("INSERT INTO Pigs (TagNumber, ...) VALUES (?, ...)");
        // stmt.setString(1, pig.getTagNumber());
        // ... sæt andre felter
        // stmt.executeUpdate();
        // Husk try-with-resources og fejlhåndtering
    }
    // Tilføj findById, update, delete metoder senere
}