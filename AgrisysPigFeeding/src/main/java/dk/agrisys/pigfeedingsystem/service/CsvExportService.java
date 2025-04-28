package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.model.FeedingRecord; // Eller hvad der nu skal eksporteres

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class CsvExportService {

    // Denne metode skal implementeres til at skrive data til en CSV-fil
    public boolean exportToCsv(List<FeedingRecord> data, File file) {
        if (data == null || file == null) {
            System.err.println("Service: CSV Export - Data eller fil er null.");
            return false;
        }
        System.out.println("Service: Simulerer eksport til CSV-fil: " + file.getName());
        System.out.println("         (RIGTIG IMPLEMENTERING MANGLER)");

        // Simpel eksempel på filskrivning (uden rigtig CSV formattering)
        try (PrintWriter writer = new PrintWriter(file)) {
            // Skriv header
            writer.println("RecordID;PigID;Timestamp;AmountKG");
            // Skriv data
            for (FeedingRecord record : data) {
                writer.printf("%d;%d;%s;%.2f%n",
                        record.getId(),
                        record.getPigId(),
                        record.getTimestamp().toString(), // Simpel format
                        record.getAmountKg());
            }
            System.out.println("Service: CSV Eksport (simuleret) succesfuld.");
            return true;
        } catch (Exception e) {
            System.err.println("Service: Fejl under CSV eksport: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}