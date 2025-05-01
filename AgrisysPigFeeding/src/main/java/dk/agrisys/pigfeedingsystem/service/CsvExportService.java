package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.model.FeedingRecord;

import java.io.File;
import java.io.FileWriter; // Use FileWriter for character-based output
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale; // To ensure correct decimal separator (.)

// Author: [Your Name/Group Name]
public class CsvExportService {

    // DateTimeFormatter to ensure a consistent date/time format
    // Choose the format that best suits CSV standards (e.g., ISO)
    // If using LocalDate:
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // If using LocalDateTime:
    // private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Exports a list of FeedingRecord objects to a CSV file.
     *
     * @param data The list of FeedingRecord objects to export.
     * @param file The file to write to.
     * @return true if the export was successful, false otherwise.
     */
    public boolean exportToCsv(List<FeedingRecord> data, File file) {
        if (data == null || file == null) {
            System.err.println("Service: CSV Export - Data or file must not be null.");
            return false;
        }

        // Use try-with-resources to ensure the PrintWriter is closed correctly
        // Use FileWriter to specify character set if needed (default is often ok)
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) { // Use FileWriter for better control

            // Write header - use a clear separator (here: semicolon)
            // Ensure header names match the data
            writer.println("RecordID;PigID;Date;AmountKG"); // Adjusted header for Date

            // Write data line by line
            for (FeedingRecord record : data) {
                if (record == null) {
                    System.err.println("Service: CSV Export - Ignoring null record in data list.");
                    continue; // Skip null records
                }
                // Use Locale.US to ensure period as decimal separator
                // Format the date using the formatter
                writer.printf(Locale.US, "%d;%d;%s;%.2f%n", // Locale.US for '.' decimal
                        record.getLocation(),
                        record.getPigId(),
                        (record.getTimestamp() != null ? record.getTimestamp() : ""), // Handle null date
                        record.getAmountInGrams());
            }

            System.out.println("Service: CSV Export to '" + file.getAbsolutePath() + "' successful. Number of records: " + data.size());
            return true;

        } catch (IOException e) {
            // Log a more specific error for file IO
            System.err.println("Service: Error writing to CSV file '" + file.getAbsolutePath() + "': " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            return false;
        } catch (Exception e) {
            // Catch other unexpected errors
            System.err.println("Service: Unexpected error during CSV export: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}