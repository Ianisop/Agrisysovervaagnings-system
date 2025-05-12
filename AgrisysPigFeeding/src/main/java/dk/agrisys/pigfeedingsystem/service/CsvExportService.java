package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CsvExportService {

    public boolean exportToExcel(List<Pig> pigs, List<FeedingRecord> feedingRecords, String filePath) {
        if (pigs.isEmpty() && feedingRecords.isEmpty()) {
            System.err.println("Service: No data to export.");
            return false;
        }

        System.out.println("Service: Exporting " + pigs.size() + " pigs and " + feedingRecords.size() + " feeding records to Excel.");

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create sheets
            Sheet pigSheet = workbook.createSheet("Pigs");
            Sheet feedingSheet = workbook.createSheet("Feeding Records");

            // Add headers for pigs
            Row pigHeaderRow = pigSheet.createRow(0);
            pigHeaderRow.createCell(0).setCellValue("Pig ID");
            pigHeaderRow.createCell(1).setCellValue("Location");
            pigHeaderRow.createCell(2).setCellValue("FCR");
            pigHeaderRow.createCell(3).setCellValue("Start Weight");
            pigHeaderRow.createCell(4).setCellValue("End Weight");
            pigHeaderRow.createCell(5).setCellValue("Weight Gain");
            pigHeaderRow.createCell(6).setCellValue("Feed Intake");
            pigHeaderRow.createCell(7).setCellValue("Test Days");

            // Write pig data
            DataFormat format = workbook.createDataFormat();
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(format.getFormat("0")); // Prevent scientific notation

            int pigRowIndex = 1;
            for (Pig pig : pigs) {
                Row row = pigSheet.createRow(pigRowIndex++);
                Cell pigIdCell = row.createCell(0);
                pigIdCell.setCellValue(pig.getTagNumber());
                pigIdCell.setCellStyle(numberStyle); // Apply number style
                row.createCell(1).setCellValue(pig.getLocation());
                row.createCell(2).setCellValue(pig.getFCR());
                row.createCell(3).setCellValue(pig.getStartWeight());
                row.createCell(4).setCellValue(pig.getEndWeight());
                row.createCell(5).setCellValue(pig.getWeightGain());
                row.createCell(6).setCellValue(pig.getFeedIntake());
                row.createCell(7).setCellValue(pig.getTestDays());
            }

            // Add headers for feeding records
            Row feedingHeaderRow = feedingSheet.createRow(0);
            feedingHeaderRow.createCell(0).setCellValue("Pig ID");
            feedingHeaderRow.createCell(1).setCellValue("Location");
            feedingHeaderRow.createCell(2).setCellValue("Amount (grams)");
            feedingHeaderRow.createCell(3).setCellValue("Timestamp");
            feedingHeaderRow.createCell(4).setCellValue("Duration");

            // Write feeding record data
            int feedingRowIndex = 1;
            for (FeedingRecord record : feedingRecords) {
                Row row = feedingSheet.createRow(feedingRowIndex++);
                Cell pigIdCell = row.createCell(0);
                pigIdCell.setCellValue(record.getPigId().toString());
                pigIdCell.setCellStyle(numberStyle); // Apply number style
                row.createCell(1).setCellValue(record.getLocation());
                row.createCell(2).setCellValue(record.getAmountInGrams());
                row.createCell(3).setCellValue(record.getTimestamp().toString());
                row.createCell(4).setCellValue(record.getDuration().toString());
            }

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

            System.out.println("Service: Data exported successfully to " + filePath);
            return true;

        } catch (IOException e) {
            System.err.println("Service: Error exporting data to Excel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}