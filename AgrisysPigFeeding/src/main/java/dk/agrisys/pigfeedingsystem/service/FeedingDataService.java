package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.dao.FeedingRecordDAO;
import dk.agrisys.pigfeedingsystem.dao.PigDAO;
import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FeedingDataService {

    private final PigDAO pigDAO;
    private final FeedingRecordDAO feedingRecordDAO;
    private final ExcelImportService excelImportService;
    private final CsvExportService csvExportService;

    public FeedingDataService(PigDAO pigDAO, FeedingRecordDAO feedingRecordDAO,
                              ExcelImportService excelImportService, CsvExportService csvExportService) {
        this.pigDAO = Objects.requireNonNull(pigDAO, "PigDAO must not be null");
        this.feedingRecordDAO = Objects.requireNonNull(feedingRecordDAO, "FeedingRecordDAO must not be null");
        this.excelImportService = Objects.requireNonNull(excelImportService, "ExcelImportService must not be null");
        this.csvExportService = Objects.requireNonNull(csvExportService, "CsvExportService must not be null");
    }

    public FeedingDataService() {
        this.pigDAO = new PigDAO();
        this.feedingRecordDAO = new FeedingRecordDAO();
        this.excelImportService = new ExcelImportService(this.feedingRecordDAO);
        this.csvExportService = new CsvExportService();
    }

    public List<Pig> getAllPigs() {
        try {
            return pigDAO.getAllPigs();
        } catch (Exception e) {
            System.err.println("Service: Error retrieving all pigs: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<FeedingRecord> getAllFeedingRecords() {
        try {
            return feedingRecordDAO.getAllFeedingRecords();
        } catch (Exception e) {
            System.err.println("Service: Error retrieving all feeding records: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public boolean exportFeedingDataToCsv(List<Pig> pigs, List<FeedingRecord> dataToExport, File file) {
        if (file == null) {
            System.err.println("Service: exportFeedingDataToCsv called with null File object.");
            return false;
        }
        if (pigs == null || dataToExport == null) {
            System.err.println("Service: exportFeedingDataToCsv called with null data list.");
            return false;
        }
        System.out.println("Service: Starting export of " + dataToExport.size() + " feeding records and " + pigs.size() + " pigs to CSV file: " + file.getAbsolutePath());
        return csvExportService.exportToExcel(pigs, dataToExport, file.getAbsolutePath());
    }

    public boolean exportAllFeedingDataToCsv(File file) {
        List<Pig> allPigs = getAllPigs();
        List<FeedingRecord> allRecords = getAllFeedingRecords();
        if (allPigs.isEmpty() && allRecords.isEmpty()) {
            System.out.println("Service: No data found to export.");
            return false;
        }
        return exportFeedingDataToCsv(allPigs, allRecords, file);
    }
}