package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.dao.FeedingRecordDAO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExcelImportService {

    private final FeedingRecordDAO feedingRecordDAO;
    // Primary expected format based on screenshot display
    private static final DateTimeFormatter DATE_FORMAT_DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Format that might include time (seen in formula bar example)
    private static final DateTimeFormatter DATE_TIME_FORMAT_FULL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");


    public ExcelImportService(FeedingRecordDAO feedingRecordDAO) {
        this.feedingRecordDAO = Objects.requireNonNull(feedingRecordDAO, "FeedingRecordDAO must not be null");
    }

    // Default constructor remains, uses its own DAO instance
    public ExcelImportService() {
        this.feedingRecordDAO = new FeedingRecordDAO();
        System.out.println("INFO: ExcelImportService created using default constructor. Dependency Injection is recommended.");
    }

    public boolean importFromExcel(File file) {
        new Thread(() -> {
            if (file == null || !file.exists() || !file.canRead()) {
                System.err.println("Excel Import Error - File is invalid or cannot be read: "
                        + (file != null ? file.getAbsolutePath() : "null"));
                return;
            }

            System.out.println("INFO: Starting Excel import from file: " + file.getName());

            List<FeedingRecord> recordsToSave = new ArrayList<>();
            boolean importHasErrors = false;
            int rowNum = 0;
            int processedDataRows = 0;
            int batchSize = 500;
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(1); // TODO: make sheet selection dynamic
                if (sheet == null) {
                    System.err.println("Excel Import Error - Sheet 1 not found in file: " + file.getName());
                    return;
                }

                boolean skipHeader = true;

                for (Row row : sheet) {
                    rowNum++;
                    if (skipHeader) {
                        System.out.println("DEBUG: Skipping header row (Row " + rowNum + ")");
                        skipHeader = false;
                        continue;
                    }

                    if (isRowEmpty(row)) {
                        System.out.println("DEBUG: Skipping empty row " + rowNum);
                        continue;
                    }

                    final int LOCATION_COL = 1;   // Column B
                    final int PIG_ID_COL = 2;     // Column C
                    final int TIMESTAMP_COL = 3;  // Column D
                    final int DURATION_COL = 4;   // Column E
                    final int AMOUNT_COL = 5;     // Column F

                    Integer location = readIntegerCell(row.getCell(LOCATION_COL), rowNum, LOCATION_COL + 1);
                    Long pigId = readLongCell(row.getCell(PIG_ID_COL), rowNum, PIG_ID_COL + 1);
                    LocalDateTime timestamp = readDateTimeCell(row.getCell(TIMESTAMP_COL), rowNum, TIMESTAMP_COL + 1);
                    LocalDateTime duration = readDateTimeCell(row.getCell(DURATION_COL), rowNum, DURATION_COL + 1);
                    Double amount = readDoubleCell(row.getCell(AMOUNT_COL), rowNum, AMOUNT_COL + 1);

                    if (location == null || pigId == null || timestamp == null || duration == null || amount == null) {
                        System.err.printf("WARN: Invalid or incomplete data at Row %d. Skipping. Data: [Loc: %s, PigID: %s, Timestamp: %s, Duration: %s, Amount: %s]%n",
                                rowNum, location, pigId, timestamp, duration, amount);
                        importHasErrors = true;
                        continue;
                    }

                    FeedingRecord record = new FeedingRecord(String.valueOf(location), pigId, timestamp, duration, amount);
                    recordsToSave.add(record);
                    processedDataRows++;
                }

                System.out.printf("INFO: Parsed %d valid records from Excel. Attempting to save...%n", recordsToSave.size());

                feedingRecordDAO.batchInsertFeedingRecords(recordsToSave); // batch save


                if (importHasErrors) {
                    System.err.println("ERROR: Excel Import completed with errors.");
                } else {
                    System.out.println("INFO: Excel Import completed successfully.");
                }

            } catch (IOException e) {
                System.err.println("ERROR: IO Error reading Excel file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("ERROR: Unexpected error during Excel import from " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        return true; // Immediately return true (import is async)
    }


    // Helper to get cell value as String, handling nulls and formatting numbers
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        DataFormatter formatter = new DataFormatter();
        // Format cell value to handle different types consistently as string
        String value = formatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }


  // Reading numeric cells helper methods below

    private Integer readIntegerCell(Cell cell, int rowNum, int colNumForMsg) {
        String cellValue = getCellStringValue(cell);
        if (cellValue == null) return null;
        try {
            // Use Double.parseDouble first to handle potential decimals then cast
            return (int) Double.parseDouble(cellValue);
        } catch (NumberFormatException e) {
            System.err.printf("ERROR: Integer parsing error at R%d C%d: Value '%s'. Error: %s%n",
                    rowNum, colNumForMsg, cellValue, e.getMessage());
            return null;
        }
    }

    private Long readLongCell(Cell cell, int rowNum, int colNumForMsg) {
        String cellValue = getCellStringValue(cell);
        if (cellValue == null) return null;
        try {
            // Use Double.parseDouble first for robustness against formats like "9.84000009083212E14" or "123.0"
            return (long) Double.parseDouble(cellValue);
        } catch (NumberFormatException e) {
            System.err.printf("ERROR: Long parsing error at R%d C%d: Value '%s'. Error: %s%n",
                    rowNum, colNumForMsg, cellValue, e.getMessage());
            return null;
        }
    }

    private LocalDateTime readDateTimeCell(Cell cell, int rowNum, int colNumForMsg) {
        if (cell == null) return null;
        String cellValueForError = "<initial>"; // For logging in case of early error

        try {
           //Check if Excel treats it as a date/time natively
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                try {
                    // This is the most reliable way if Excel knows it's a date
                    return cell.getLocalDateTimeCellValue();
                } catch (Exception poiEx) {
                    System.err.printf("WARN: POI failed to read native date at R%d C%d. Will try string parsing. POI Error: %s%n",
                            rowNum, colNumForMsg, poiEx.getMessage());
                    // Fall through to string parsing
                }
            }

            //If not a native Excel date, try parsing the cell's string value
            String cellValue = getCellStringValue(cell);
            cellValueForError = cellValue; // Update value for potential error message
            if (cellValue == null) return null;

            // Try parsing with full date and time first
            try {
                return LocalDateTime.parse(cellValue, DATE_TIME_FORMAT_FULL);
            } catch (DateTimeParseException e1) {
                // If full format fails, try parsing as just date and set time to start of day
                try {
                    LocalDate date = LocalDate.parse(cellValue, DATE_FORMAT_DISPLAY);
                    return date.atStartOfDay();
                } catch (DateTimeParseException e2) {
                    // If both specific formats fail, report error
                    System.err.printf("ERROR: Date/Time parsing error at R%d C%d: Could not parse value '%s' using formats '%s' or '%s'. Error: %s%n",
                            rowNum, colNumForMsg, cellValue, DATE_TIME_FORMAT_FULL.toString(), DATE_FORMAT_DISPLAY.toString(), e2.getMessage());
                    return null;
                }
            }
        } catch (Exception e) { // Catch potential errors from POI or unexpected issues
            System.err.printf("ERROR: Unexpected Date/Time parsing error at R%d C%d: Value approx '%s'. Error: %s%n",
                    rowNum, colNumForMsg, cellValueForError, e.getMessage());
            // e.printStackTrace();
            return null;
        }
    }


    private Float readFloatCell(Cell cell, int rowNum, int colNumForMsg) {
        if (cell == null) return null;

        String cellValueStr = null; // For error messages

        try {
            // Check if Excel stores it as a numeric time value (fraction of a day)
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                // Excel time is stored as fraction of a day. Convert to seconds.
                double excelTimeValue = cell.getNumericCellValue();
                // 1 day = 24 * 60 * 60 = 86400 seconds
                double totalSeconds = excelTimeValue * 86400.0;
                System.out.printf("DEBUG: R%d C%d - Parsed Excel numeric time %f as %.1f seconds%n",
                        rowNum, colNumForMsg, excelTimeValue, totalSeconds);
                return (float) totalSeconds;
            }

            //Try parsing as a string (handles MM:SS and plain numbers)
            cellValueStr = getCellStringValue(cell);
            if (cellValueStr == null) return null;

            // Handle MM:SS format (or HH:MM:SS)
            if (cellValueStr.contains(":")) {
                String[] parts = cellValueStr.split(":");
                float totalSeconds = 0;
                try {
                    if (parts.length == 2) { // MM:SS
                        int minutes = Integer.parseInt(parts[0]);
                        int seconds = Integer.parseInt(parts[1]);
                        totalSeconds = (minutes * 60.0f) + seconds;
                        System.out.printf("DEBUG: R%d C%d - Parsed MM:SS string '%s' as %.1f seconds%n",
                                rowNum, colNumForMsg, cellValueStr, totalSeconds);
                    } else if (parts.length == 3) { // HH:MM:SS
                        int hours = Integer.parseInt(parts[0]);
                        int minutes = Integer.parseInt(parts[1]);
                        int seconds = Integer.parseInt(parts[2]);
                        totalSeconds = (hours * 3600.0f) + (minutes * 60.0f) + seconds;
                        System.out.printf("DEBUG: R%d C%d - Parsed HH:MM:SS string '%s' as %.1f seconds%n",
                                rowNum, colNumForMsg, cellValueStr, totalSeconds);
                    } else {
                        System.err.printf("ERROR: Unexpected time format at R%d C%d: Value '%s'%n",
                                rowNum, colNumForMsg, cellValueStr);
                        return null;
                    }
                    return totalSeconds;
                } catch (NumberFormatException nfe) {
                    System.err.printf("ERROR: Could not parse time components as numbers at R%d C%d: Value '%s'. Error: %s%n",
                            rowNum, colNumForMsg, cellValueStr, nfe.getMessage());
                    return null;
                }
            } else {
                // Handle plain number format
                try {
                    // Replace comma just in case, although less likely for duration
                    float durationValue = Float.parseFloat(cellValueStr.replace(',', '.'));
                    System.out.printf("DEBUG: R%d C%d - Parsed plain number string '%s' as %.1f%n",
                            rowNum, colNumForMsg, cellValueStr, durationValue);
                    // Assuming SECONDS here. If it's minutes, multiply by 60.
                    return durationValue;
                } catch (NumberFormatException nfe) {
                    System.err.printf("ERROR: Float parsing error (plain number) at R%d C%d: Value '%s'. Error: %s%n",
                            rowNum, colNumForMsg, cellValueStr, nfe.getMessage());
                    return null;
                }
            }
        } catch (Exception e) { // Catch other potential errors
            System.err.printf("ERROR: Unexpected Float/Duration parsing error at R%d C%d: Approx Value '%s'. Error: %s%n",
                    rowNum, colNumForMsg, cellValueStr != null ? cellValueStr : "<cell read error>", e.getMessage());
            // e.printStackTrace(); // Uncomment for more details if needed
            return null;
        }
    }

    private Double readDoubleCell(Cell cell, int rowNum, int colNumForMsg) {
        String cellValue = getCellStringValue(cell);
        if (cellValue == null) return null;
        try {
            // Replace comma decimal separator with dot if necessary
            return Double.parseDouble(cellValue.replace(',', '.'));
        } catch (NumberFormatException e) {
            System.err.printf("ERROR: Double parsing error at R%d C%d: Value '%s'. Error: %s%n",
                    rowNum, colNumForMsg, cellValue, e.getMessage());
            return null;
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        // Use DataFormatter to check if any cell renders as non-empty text
        DataFormatter formatter = new DataFormatter();
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = formatter.formatCellValue(cell).trim();
                if (!value.isEmpty()) {
                    return false; // Found a non-empty cell
                }
            }
        }
        return true; // No non-empty cells found
    }
}