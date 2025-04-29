package dk;

import dk.agrisys.pigfeedingsystem.dao.DatabaseConnector;
import dk.agrisys.pigfeedingsystem.dao.FeedingRecordDAO;
import dk.agrisys.pigfeedingsystem.service.ExcelImportService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Test database connection
        System.out.println("Testing database driver...");
        DatabaseConnector.testConnection();
        System.out.println("Database driver test completed.");

        // Start with the login view
        loadScene("view/LoginView.fxml");

        stage.setTitle("Agrisys Pig Feeding System");
        stage.show();
    }

    // Method to switch scenes
    public static void loadScene(String fxmlFile) throws IOException {
        // Ensure the path is relative to the 'resources' folder
        if (!fxmlFile.startsWith("/")) {
            fxmlFile = "/" + fxmlFile.replace("dk/agrisys/", "");
        }

        URL fxmlUrl = App.class.getResource(fxmlFile);
        if (fxmlUrl == null) {
            System.err.println("Error: Could not find FXML file: " + fxmlFile);
            throw new IOException("Cannot load FXML file: " + fxmlFile);
        }
        System.out.println("Loading FXML: " + fxmlUrl);

        Parent root = FXMLLoader.load(fxmlUrl);
        Scene scene = new Scene(root);

        // Apply CSS if the file exists
        URL cssUrl = App.class.getResource("/css/styles.css");
        if (cssUrl != null) {
            System.out.println("Loading CSS: " + cssUrl);
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("CSS file /css/styles.css not found.");
        }

        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Static nested class for testing Excel import
    public static class ImportTester {

        public static void main(String[] args) {
            System.out.println("Starting Excel import test...");

            // Specify the path to your Excel file
            String filePath = "PPT data.xlsx";

            File excelFile = new File(filePath);

            // Check if the file exists
            if (!excelFile.exists()) {
                System.err.println("Error: Excel file not found at path: " + excelFile.getAbsolutePath());
                return;
            } else {
                System.out.println("Excel file found: " + excelFile.getAbsolutePath());
            }

            // Create instances of DAO and Service
            FeedingRecordDAO dao = new FeedingRecordDAO();
            ExcelImportService importService = new ExcelImportService();

            // Call the import method
            System.out.println("Attempting to import data...");
            boolean importSuccess = false;
            try {
                importSuccess = importService.importFromExcel(excelFile);

                // Check the result
                if (importSuccess) {
                    System.out.println("Import process completed. Check console output and database for results.");
                } else {
                    System.err.println("Import process failed or completed with errors. Check preceding error messages.");
                }
            } catch (Exception e) {
                System.err.println("An unexpected error occurred during the import process:");
                e.printStackTrace();
            }

            System.out.println("Excel import test finished.");
        }
    }
}