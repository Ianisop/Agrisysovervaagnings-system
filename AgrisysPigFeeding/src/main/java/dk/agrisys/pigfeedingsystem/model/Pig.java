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
        return FCR != null ? FCR : 0.0f;
    }


    public Float getStartWeight() {
        return StartWeight;
    }


    public Float getWeightGain() {
        return WeightGain;
    }


    public Float getFeedIntake() {
        return FeedIntake;
    }


    public int getTestDays() {
        return TestDays;
    }

    public Float getEndWeight() {
        return EndWeight;
    }

    public float getDuration() {
        return Duration != null ? Duration : 0;
    }

    // Tilføj flere felter som vægt, alder etc. senere

    public Pig(String tagNumber) {

        this.tagNumber = tagNumber;
    }
    public Pig(Integer location, String tagNumber, Float fcr, Float startWeight, Float totalFeedIntake, Float weightGain, Float endWeight, Integer completedDays) {

        this.Location = location;
        this.tagNumber = tagNumber;
        this.FCR = fcr;
        this.StartWeight = startWeight;
        this.FeedIntake = totalFeedIntake;
        this.WeightGain = weightGain;
        this.EndWeight = endWeight;
        this.TestDays = completedDays;

    }

    public String getTagNumber() { return tagNumber; }

    @Override
    public String toString() { // Nyttig til simple lister
        return "Gris #" + tagNumber;
    }
}