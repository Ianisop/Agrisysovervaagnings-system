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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExcelImportService {

    private final FeedingRecordDAO feedingRecordDAO;
    private static final DateTimeFormatter EXPECTED_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ExcelImportService(FeedingRecordDAO feedingRecordDAO) {
        this.feedingRecordDAO = Objects.requireNonNull(feedingRecordDAO, "FeedingRecordDAO must not be null");
    }

    public ExcelImportService() {
        this.feedingRecordDAO = new FeedingRecordDAO();
    }

    public boolean importFromExcel(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            System.err.println("Service: Excel Import - File not found, cannot be read, or is null: " + (file != null ? file.getAbsolutePath() : "null"));
            return false;
        }

        List<FeedingRecord> recordsToSave = new ArrayList<>();
        boolean importHasErrors = false;
        int rowNum = 0;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                System.err.println("Service: Excel Import - Could not find the first sheet in the file: " + file.getName());
                return false;
            }

            boolean skipHeader = true;
            for (Row row : sheet) {
                rowNum++;
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                if (isRowEmpty(row)) {
                    System.out.println("Service: Excel Import - Ignoring empty row " + rowNum);
                    continue;
                }

                try {
                    Integer pigId = readIntegerCell(row.getCell(0), rowNum, 0);
                    LocalDate date = readDateCell(row.getCell(1), rowNum, 1);
                    Double amount = readDoubleCell(row.getCell(2), rowNum, 2);

                    if (pigId == null || date == null || amount == null) {
                        System.err.printf("Service: Excel Import - Invalid data in row %d. PigID: %s, Date: %s, Amount: %s. Row skipped.%n",
                                rowNum, pigId, date, amount);
                        importHasErrors = true;
                        continue;
                    }

                    FeedingRecord record = new FeedingRecord(   0, pigId, LocalDateTime.of(date, LocalDateTime.now().toLocalTime()), amount);
                    record.setPigId(pigId);
                    record.setDate(date);
                    record.setAmountKg(amount);

                    recordsToSave.add(record);

                } catch (Exception e) {
                    System.err.printf("Service: Excel Import - Error processing row %d: %s. Row skipped.%n", rowNum, e.getMessage());
                    e.printStackTrace();
                    importHasErrors = true;
                }
            }

            if (!recordsToSave.isEmpty()) {
                System.out.println("Service: Excel Import - Attempting to save " + recordsToSave.size() + " records...");
                int savedCount = 0;
                for (FeedingRecord record : recordsToSave) {
                    try {
                        boolean saved = feedingRecordDAO.saveFeedingRecord(record);
                        if (saved) {
                            savedCount++;
                        } else {
                            System.err.println("Service: Excel Import - DAO returned false for record: PigID=" + record.getPigId() + ", Date=" + record.getDate());
                            importHasErrors = true;
                        }
                    } catch (Exception e) {
                        System.err.println("Service: Excel Import - Error saving record via DAO: " + e.getMessage());
                        importHasErrors = true;
                    }
                }
                System.out.println("Service: Excel Import - " + savedCount + " out of " + recordsToSave.size() + " records were saved.");
            } else {
                System.out.println("Service: Excel Import - No valid records found to save.");
            }

            System.out.println("Service: Excel Import from '" + file.getName() + "' completed." + (importHasErrors ? " Errors occurred during the process." : ""));
            return true;

        } catch (IOException e) {
            System.err.println("Service: Excel Import - IO Error reading file '" + file.getAbsolutePath() + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Service: Excel Import - Unexpected error during import from '" + file.getName() + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Integer readIntegerCell(Cell cell, int rowNum, int colNum) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double value = cell.getNumericCellValue();
                if (value == Math.floor(value)) {
                    return (int) value;
                } else {
                    System.err.printf("Warning R%d C%d: Numeric value is not an integer (%f)%n", rowNum, colNum + 1, value);
                    return null;
                }
            } else if (cell.getCellType() == CellType.STRING) {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } else {
                System.err.printf("Warning R%d C%d: Unexpected cell type (%s) for integer%n", rowNum, colNum + 1, cell.getCellType());
                return null;
            }
        } catch (NumberFormatException | IllegalStateException e) {
            System.err.printf("Error R%d C%d: Could not parse integer from '%s'%n", rowNum, colNum + 1, cell.toString());
            return null;
        }
    }

    private Double readDoubleCell(Cell cell, int rowNum, int colNum) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String val = cell.getStringCellValue().trim().replace(',', '.');
                return Double.parseDouble(val);
            } else {
                System.err.printf("Warning R%d C%d: Unexpected cell type (%s) for double%n", rowNum, colNum + 1, cell.getCellType());
                return null;
            }
        } catch (NumberFormatException | IllegalStateException e) {
            System.err.printf("Error R%d C%d: Could not parse double from '%s'%n", rowNum, colNum + 1, cell.toString());
            return null;
        }
    }

    private LocalDate readDateCell(Cell cell, int rowNum, int colNum) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDateTime dateTime = cell.getLocalDateTimeCellValue();
                    return dateTime.toLocalDate();
                } else {
                    System.err.printf("Warning R%d C%d: Numeric cell not formatted as date.%n", rowNum, colNum + 1);
                    return null;
                }
            } else if (cell.getCellType() == CellType.STRING) {
                String dateString = cell.getStringCellValue().trim();
                try {
                    return LocalDate.parse(dateString, EXPECTED_DATE_FORMAT);
                } catch (DateTimeParseException e) {
                    System.err.printf("Error R%d C%d: Could not parse date string '%s' with format '%s'. %s%n",
                            rowNum, colNum + 1, dateString, "yyyy-MM-dd", e.getMessage());
                    return null;
                }
            } else {
                System.err.printf("Warning R%d C%d: Unexpected cell type (%s) for date%n", rowNum, colNum + 1, cell.getCellType());
                return null;
            }
        } catch (IllegalStateException e) {
            System.err.printf("Error R%d C%d: Could not read date from cell (%s)%n", rowNum, colNum + 1, cell.getCellType());
            return null;
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}