package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.dao.FeedingRecordDAO;
import dk.agrisys.pigfeedingsystem.dao.PigDAO;
import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;

import java.io.File;
import java.util.Collections; // For Collections.emptyList()
import java.util.List;
import java.util.Objects; // For Objects.requireNonNull

// Author: [Your Name/Group Name]
public class FeedingDataService {

    // Dependencies - Ideally injected via constructor
    private final PigDAO pigDAO;
    private final FeedingRecordDAO feedingRecordDAO;
    private final ExcelImportService excelImportService;
    private final CsvExportService csvExportService; // Added CsvExportService

    /**
     * Constructor for Dependency Injection (recommended).
     * Ensures all necessary services and DAOs are provided.
     */
    public FeedingDataService(PigDAO pigDAO, FeedingRecordDAO feedingRecordDAO,
                              ExcelImportService excelImportService, CsvExportService csvExportService) {
        this.pigDAO = Objects.requireNonNull(pigDAO, "PigDAO must not be null");
        this.feedingRecordDAO = Objects.requireNonNull(feedingRecordDAO, "FeedingRecordDAO must not be null");
        this.excelImportService = Objects.requireNonNull(excelImportService, "ExcelImportService must not be null");
        this.csvExportService = Objects.requireNonNull(csvExportService, "CsvExportService must not be null");
    }

    /**
     * Simple constructor that instantiates dependencies itself.
     * Easier to start with, but less flexible and harder to test.
     * Requires ExcelImportService to have a constructor that takes FeedingRecordDAO,
     * or a default constructor that creates its own DAO.
     */
    public FeedingDataService() {
        this.pigDAO = new PigDAO();
        this.feedingRecordDAO = new FeedingRecordDAO();
        // Ensure ExcelImportService's constructor can handle this
        // If ExcelImportService takes a DAO in its constructor:
        this.excelImportService = new ExcelImportService(this.feedingRecordDAO);
        // If ExcelImportService has a default constructor (like in the original code):
        // this.excelImportService = new ExcelImportService(); // This only works if ExcelImportService creates its own DAO

        this.csvExportService = new CsvExportService(); // CsvExportService has no dependencies
    }


    /**
     * Gets all pigs from the database.
     * @return A list of Pig objects. Returns an empty list on error or if none are found.
     */
    public List<Pig> getAllPigs() {
        try {
            return pigDAO.getAllPigs();
        } catch (Exception e) {
            System.err.println("Service: Error retrieving all pigs: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // Return empty list on error
        }
    }

    /**
     * Gets all feeding records from the database.
     * @return A list of FeedingRecord objects. Returns an empty list on error or if none are found.
     */
    public List<FeedingRecord> getAllFeedingRecords() {
        try {
            return feedingRecordDAO.getAllFeedingRecords();
        } catch (Exception e) {
            System.err.println("Service: Error retrieving all feeding records: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // Return empty list on error
        }
    }

    /**
     * Gets all feeding records for a specific pig via its ID.
     * @param pig The pig whose feedings should be retrieved. Can be null.
     * @return A list of FeedingRecord objects for the pig. Returns an empty list if the pig is null,
     *         lacks a valid ID, no records are found, or an error occurs.
     */
    public List<FeedingRecord> getFeedingsForPig(Pig pig) {
        // Check for null pig or invalid ID early
        if (pig == null) { // Assuming ID > 0 is valid
            if (pig == null) System.err.println("Service: getFeedingsForPig called with null Pig object.");
            else System.err.println("Service: getFeedingsForPig called with Pig object without valid ID: " + pig.getTagNumber());
            return Collections.emptyList(); // Use Collections.emptyList() for an immutable empty list
        }
        try {
            return feedingRecordDAO.getFeedingsForPig(Integer.parseInt(pig.getTagNumber())); // FIX LATER
        } catch (Exception e) {
            System.err.printf("Service: Error retrieving feedings for pig ID %d: %s%n", pig.getTagNumber(), e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // Return empty list on error
        }
    }

    /**
     * Imports feeding data from the specified Excel file.
     * Delegates the call to ExcelImportService.
     * @param file The Excel file to import.
     * @return true if the import started successfully (the result depends on the import service), false otherwise.
     */
    public boolean importFeedingDataFromExcel(File file) {
        if (file == null) {
            System.err.println("Service: importFeedingDataFromExcel called with null File object.");
            return false;
        }
        System.out.println("Service: Starting import from Excel file: " + file.getAbsolutePath());
        // Delegates the work to the import service
        return excelImportService.importFromExcel(file);
    }

    /**
     * Exports feeding data to the specified CSV file.
     * Delegates the call to CsvExportService.
     *
     * @param file The CSV file to write to.
     * @param dataToExport The list of FeedingRecord objects to export. Can be, for example, the result of getAllFeedingRecords().
     * @return true if the export was successful, false otherwise.
     */
    public boolean exportFeedingDataToCsv(List<FeedingRecord> dataToExport, File file) {
        if (file == null) {
            System.err.println("Service: exportFeedingDataToCsv called with null File object.");
            return false;
        }
        if (dataToExport == null) {
            System.err.println("Service: exportFeedingDataToCsv called with null data list.");
            return false;
        }
        System.out.println("Service: Starting export of " + dataToExport.size() + " records to CSV file: " + file.getAbsolutePath());
        // Delegates the work to the export service
        return csvExportService.exportToCsv(dataToExport, file);
    }

    /**
     * Overload method for easy export of *all* feeding data.
     * Retrieves all records itself before exporting.
     *
     * @param file The CSV file to write to.
     * @return true if the export was successful, false otherwise.
     */
    public boolean exportAllFeedingDataToCsv(File file) {
        List<FeedingRecord> allRecords = getAllFeedingRecords();
        // Check if retrieval failed (returned empty list, but error might have been logged)
        if (allRecords.isEmpty()) {
            System.out.println("Service: No feeding data found to export.");
            // Decide whether exporting nothing is success (true) or failure (false).
            // Let's consider exporting an empty list (potentially creating an empty file with header) as "success".
            return csvExportService.exportToCsv(allRecords, file); // Export empty list (creates header)
        }
        return exportFeedingDataToCsv(allRecords, file);
    }

}