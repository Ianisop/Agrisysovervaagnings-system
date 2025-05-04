package dk.agrisys.pigfeedingsystem.model;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class Pig {
    private String tagNumber;
    // Tilføj flere felter som vægt, alder etc. senere

    public Pig(String tagNumber) {

        this.tagNumber = tagNumber;
    }

    public String getTagNumber() { return tagNumber; }

    @Override
    public String toString() { // Nyttig til simple lister
        return "Gris #" + tagNumber;
    }
}