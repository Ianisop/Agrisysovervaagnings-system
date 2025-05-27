package dk.agrisys.pigfeedingsystem.model;

/**
 * Model class representing a pig.
 * Contains information about the pig's ID, location, weight, feed intake, and other relevant data.
 */
public class Pig {
    private String tagNumber; // The pig's unique tag number
    private int id; // The pig's ID
    private int Number; // The number assigned to the pig
    private int Location; // The location of the pig
    private Float FCR; // Feed Conversion Ratio (FCR) of the pig
    private Float StartWeight; // The starting weight of the pig
    private Float EndWeight; // The ending weight of the pig
    private Float WeightGain; // The weight gain of the pig
    private Float FeedIntake; // The feed intake of the pig
    private int TestDays; // The number of test days for the pig
    private Float Duration; // The duration of the test

    /**
     * Retrieves the pig's ID as a Long.
     * @return The pig's ID.
     */
    public Long getId() {
        return (long) id;
    }

    /**
     * Retrieves the pig's number.
     * @return The pig's number.
     */
    public int getNumber() {
        return Number;
    }

    /**
     * Retrieves the pig's location.
     * @return The pig's location.
     */
    public int getLocation() {
        return Location;
    }

    /**
     * Retrieves the pig's Feed Conversion Ratio (FCR).
     * @return The pig's FCR or 0.0 if FCR is null.
     */
    public float getFCR() {
        return FCR != null ? FCR : 0.0f;
    }

    /**
     * Retrieves the pig's starting weight.
     * @return The pig's starting weight.
     */
    public Float getStartWeight() {
        return StartWeight;
    }

    /**
     * Retrieves the pig's weight gain.
     * @return The pig's weight gain.
     */
    public Float getWeightGain() {
        return WeightGain;
    }

    /**
     * Retrieves the pig's feed intake.
     * @return The pig's feed intake.
     */
    public Float getFeedIntake() {
        return FeedIntake;
    }

    /**
     * Retrieves the number of test days for the pig.
     * @return The number of test days.
     */
    public int getTestDays() {
        return TestDays;
    }

    /**
     * Retrieves the pig's ending weight.
     * @return The pig's ending weight.
     */
    public Float getEndWeight() {
        return EndWeight;
    }

    /**
     * Retrieves the duration of the test.
     * @return The duration of the test or 0 if Duration is null.
     */
    public float getDuration() {
        return Duration != null ? Duration : 0;
    }

    /**
     * Constructor to create a pig with a tag number.
     * @param tagNumber The pig's tag number.
     */
    public Pig(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    /**
     * Constructor to create a pig with detailed information.
     * @param location The location of the pig.
     * @param tagNumber The pig's tag number.
     * @param fcr The Feed Conversion Ratio of the pig.
     * @param startWeight The starting weight of the pig.
     * @param totalFeedIntake The feed intake of the pig.
     * @param weightGain The weight gain of the pig.
     * @param endWeight The ending weight of the pig.
     * @param completedDays The number of completed test days.
     */
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

    /**
     * Retrieves the pig's tag number.
     * @return The pig's tag number.
     */
    public String getTagNumber() {
        return tagNumber;
    }

    /**
     * Returns a string representation of the pig.
     * @return A string representing the pig.
     */
    @Override
    public String toString() {
        return "Pig #" + tagNumber;
    }
}