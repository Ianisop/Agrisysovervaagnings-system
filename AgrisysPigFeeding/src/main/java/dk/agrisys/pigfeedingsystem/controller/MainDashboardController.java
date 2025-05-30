package dk.agrisys.pigfeedingsystem.controller;

import dk.util.SessionContext;
import dk.agrisys.pigfeedingsystem.dao.InviteCodeDAO;
import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;
import dk.agrisys.pigfeedingsystem.model.User;
import dk.agrisys.pigfeedingsystem.model.UserRole;
import dk.agrisys.pigfeedingsystem.service.CsvExportService;
import dk.agrisys.pigfeedingsystem.service.ExcelImportService;
import dk.agrisys.pigfeedingsystem.service.FeedingDataService;
import dk.util.Generator;
import dk.util.IController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Controller for the main dashboard, handling data display, charts, and user actions.
 */
public class MainDashboardController implements IController {

    @FXML
    private Tab adminTab; // Tab for administrator functions

    @FXML
    private Tab dataTab; // Tab for data display

    @FXML
    private Tab warningTab; // Tab for warnings

    @FXML
    private TextFlow dataTextLog; // Log for displaying data

    @FXML
    private Button exportCSV; // Button for exporting data as CSV

    @FXML
    private Button exportXLSX; // Button for exporting data as Excel

    @FXML
    private Text inviteCodeTextDisplay; // Text field for displaying the invite code

    @FXML
    private Button generateAdminInvite; // Button for generating admin invite code

    @FXML
    private Button generateInvite; // Button for generating user invite code

    @FXML
    private Button importCSV; // Button for importing CSV data

    @FXML
    private Button importXLSX; // Button for importing Excel data

    @FXML
    private Button copyCodeToClipboard; // Button for copying the invite code to clipboard

    @FXML
    private PieChart pieChart; // Pie chart for data visualization

    @FXML
    private LineChart<String, Number> lineChart; // Line chart for data visualization

    @FXML
    private StackedBarChart<String, Number> stackbarChart; // Stacked bar chart for data visualization

    @FXML
    private Tab kpiTab; // Tab for KPI display

    private Stage primaryStage; // Main application window

    /**
     * Sets the primary stage.
     * @param stage The main application window
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Initializes the dashboard and configures tabs and charts.
     */
    @FXML
    private void initialize() {
        User user = SessionContext.getCurrentUser(); // Retrieve the current user
        if (user == null) return;

        UserRole userRole = user.getRole(); // Retrieve the user's role
        if (userRole == null) return;

        // Enable or disable the admin tab based on the user's role
        if (userRole == UserRole.SUPERUSER) {
            adminTab.setDisable(false);
        } else {
            adminTab.setDisable(true);
            adminTab.getContent().setStyle("-fx-background-color: grey;");
        }

        // Hide legends on charts
        pieChart.setLegendVisible(false);
        lineChart.setLegendVisible(false);

        // Populate charts with data
        populateLineChart();
        populatePieChart();
        populateStackedBarChart();
    }

    /**
     * Imports data from an Excel file.
     * @param e ActionEvent from the button
     */
    public void importXLSX(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        ExcelImportService eis = new ExcelImportService();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Spreadsheets", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) eis.importFromExcel(selectedFile);
    }

    /**
     * Generates a user invite code and saves it to the database.
     * @param e ActionEvent from the button
     */
    public void generateUserInvite(ActionEvent e) {
        String code = Generator.generate(16);
        InviteCodeDAO inviteCodeDAO = new InviteCodeDAO();
        try {
            inviteCodeDAO.saveCodeToDb(code, false);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        inviteCodeTextDisplay.setText(code);
    }

    /**
     * Generates an admin invite code and saves it to the database.
     * @param e ActionEvent from the button
     */
    public void generateAdminInvite(ActionEvent e) {
        String code = Generator.generate(16);
        InviteCodeDAO inviteCodeDAO = new InviteCodeDAO();
        try {
            inviteCodeDAO.saveCodeToDb(code, true);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        inviteCodeTextDisplay.setText(code);
    }

    /**
     * Exports data to a CSV file.
     * @param event ActionEvent from the button
     */
    public void handleExportButtonClick(ActionEvent event) {
        CsvExportService csvExportService = new CsvExportService();

        // Retrieve data from the database
        List<Pig> pigs = fetchPigs();
        List<FeedingRecord> feedingRecords = fetchFeedingRecords();

        // Predefined file path
        String filePath = System.getProperty("user.home") + "/Exports/pig_feeding_data.csv";

        File file = new File(filePath);
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            System.err.println("Error: Could not create export directory.");
            return;
        }

        boolean success = csvExportService.exportToExcel(pigs, feedingRecords, filePath);
        if (success) {
            System.out.println("Export successful: " + filePath);
        } else {
            System.err.println("Export failed.");
        }
    }

    /**
     * Copies the invite code to the clipboard.
     * @param e ActionEvent from the button
     */
    public void copyCodeToClipboard(ActionEvent e) {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(
                        new StringSelection(inviteCodeTextDisplay.getText()),
                        null
                );
    }

    /**
     * Populates the line chart with data.
     */
    private void populateLineChart() {
        FeedingDataService service = new FeedingDataService();
        List<FeedingRecord> records = service.getFeedingRecords();

        if (records != null && !records.isEmpty()) {
            Map<String, Double> feedTrend = records.stream()
                    .collect(Collectors.groupingBy(
                            record -> record.getTimestamp().toLocalDate().toString(),
                            TreeMap::new,
                            Collectors.summingDouble(FeedingRecord::getAmountInGrams)
                    ));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            feedTrend.forEach((date, totalFeed) -> {
                if (totalFeed > 0) {
                    series.getData().add(new XYChart.Data<>(date, totalFeed));
                }
            });

            lineChart.getData().clear();
            lineChart.getData().add(series);

            // Adjust Y-axis to avoid forced zero point
            NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
            yAxis.setForceZeroInRange(false);
        } else {
            System.err.println("No feeding data found.");
        }
    }

    /**
     * Populates the pie chart with data.
     */
    private void populatePieChart() {
        FeedingDataService service = new FeedingDataService();
        List<FeedingRecord> records = service.getFeedingRecords();

        if (records != null && !records.isEmpty()) {
            Map<String, Double> feedDistribution = records.stream()
                    .collect(Collectors.groupingBy(
                            FeedingRecord::getLocation,
                            Collectors.summingDouble(FeedingRecord::getAmountInGrams)
                    ));

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            feedDistribution.forEach((location, totalFeed) -> pieChartData.add(new PieChart.Data(location, totalFeed)));

            pieChart.setData(pieChartData);
        }
    }

    /**
     * Populates the stacked bar chart with data.
     */
    private void populateStackedBarChart() {
        FeedingDataService service = new FeedingDataService();
        List<FeedingRecord> records = service.getFeedingRecords();

        if (records != null && !records.isEmpty()) {
            Map<String, Map<String, Double>> stackedData = records.stream()
                    .collect(Collectors.groupingBy(
                            record -> record.getTimestamp().toLocalDate().toString(),
                            Collectors.groupingBy(
                                    FeedingRecord::getLocation,
                                    Collectors.summingDouble(FeedingRecord::getAmountInGrams)
                            )
                    ));

            stackbarChart.getData().clear();
            stackedData.forEach((date, locationData) -> {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(date);
                locationData.forEach((location, totalFeed) -> series.getData().add(new XYChart.Data<>(location, totalFeed)));
                stackbarChart.getData().add(series);
            });
        }
    }

    /**
     * Retrieves all pigs from the database.
     * @return List of pigs
     */
    private List<Pig> fetchPigs() {
        FeedingDataService service = new FeedingDataService();
        return service.getAllPigs();
    }

    /**
     * Retrieves all feeding data from the database.
     * @return List of feeding data
     */
    private List<FeedingRecord> fetchFeedingRecords() {
        FeedingDataService service = new FeedingDataService();
        return service.getAllFeedingRecords(null, LocalDateTime.of(1, 1, 1, 0, 0)); // Default values
    }
}