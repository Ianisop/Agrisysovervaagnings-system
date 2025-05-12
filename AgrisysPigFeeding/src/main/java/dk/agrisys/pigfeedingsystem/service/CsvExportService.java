package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CsvExportService {

    /**
     * Exports a list of Pig and FeedingRecord objects to an Excel file with two sheets.
     *
     * @param pigs           The list of Pig objects to export.
     * @param feedingRecords The list of FeedingRecord objects to export.
     * @param filePath       The file path to write to.
     * @return true if the export was successful, false otherwise.
     */
    public boolean exportToExcel(List<Pig> pigs, List<FeedingRecord> feedingRecords, String filePath) {
        if (pigs == null || feedingRecords == null || filePath == null || filePath.isEmpty()) {
            System.err.println("Service: Excel Export - Data or file path must not be null or empty.");
            return false;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create Pig sheet
            Sheet pigSheet = workbook.createSheet("Pigs");
            createPigSheet(pigSheet, pigs);

            // Create FeedingRecords sheet
            Sheet feedingSheet = workbook.createSheet("FeedingRecords");
            createFeedingRecordSheet(feedingSheet, feedingRecords);

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            System.out.println("Service: Excel Export to '" + filePath + "' successful.");
            return true;

        } catch (IOException e) {
            System.err.println("Service: Error writing to Excel file '" + filePath + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void createPigSheet(Sheet sheet, List<Pig> pigs) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"PigID", "Number", "Location", "FCR", "StartWeight", "EndWeight", "WeightGain", "FeedIntake", "TestDays", "Duration"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (Pig pig : pigs) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(pig.getId());
            row.createCell(1).setCellValue(pig.getNumber());
            row.createCell(2).setCellValue(pig.getLocation());
            row.createCell(3).setCellValue(pig.getFCR());
            row.createCell(4).setCellValue(pig.getStartWeight());
            row.createCell(5).setCellValue(pig.getEndWeight());
            row.createCell(6).setCellValue(pig.getWeightGain());
            row.createCell(7).setCellValue(pig.getFeedIntake());
            row.createCell(8).setCellValue(pig.getTestDays());
            row.createCell(9).setCellValue(pig.getDuration());
        }
    }

    private void createFeedingRecordSheet(Sheet sheet, List<FeedingRecord> feedingRecords) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"FeedingID", "FeedingLocation", "PigID", "Date", "Duration", "FeedAmountGrams"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowNum = 1;
        for (FeedingRecord record : feedingRecords) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(record.getId());
            row.createCell(1).setCellValue(record.getLocation());
            row.createCell(2).setCellValue(record.getPigId());
            row.createCell(3).setCellValue(record.getTimestamp() != null ? record.getTimestamp().toString() : "");
            row.createCell(4).setCellValue(record.getDuration() != null ? record.getDuration().toString() : "");
            row.createCell(5).setCellValue(record.getAmountInGrams());
        }
    }
}