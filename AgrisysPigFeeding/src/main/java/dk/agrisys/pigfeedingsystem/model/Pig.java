package dk.agrisys.pigfeedingsystem.model;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class Pig {
    private String tagNumber;

    private int id;
    private int Number;
    private int Location;
    private Float FCR;
    private Float StartWeight;
    private Float EndWeight;
    private Float WeightGain;
    private Float FeedIntake;
    private int TestDays;
    private Float Duration;

    public Long getId() {
        return (long) id;
    }

    public int getNumber() {
        return Number;
    }


    public int getLocation() {
        return Location;
    }


    public float getFCR() {
        return FCR;
    }


    public float getStartWeight() {
        return StartWeight;
    }


    public float getWeightGain() {
        return WeightGain;
    }


    public float getFeedIntake() {
        return FeedIntake;
    }


    public int getTestDays() {
        return TestDays;
    }

    public float getEndWeight() {
        return EndWeight;
    }

   public float getDuration() {
        return Duration;
   }

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