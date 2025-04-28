package dk.agrisys.pigfeedingsystem.model;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class Pig {
    private int id;
    private String tagNumber;
    // Tilføj flere felter som vægt, alder etc. senere

    public Pig(int id, String tagNumber) {
        this.id = id;
        this.tagNumber = tagNumber;
    }

    public int getId() { return id; }
    public String getTagNumber() { return tagNumber; }

    @Override
    public String toString() { // Nyttig til simple lister
        return "Gris #" + tagNumber + " (ID: " + id + ")";
    }
}