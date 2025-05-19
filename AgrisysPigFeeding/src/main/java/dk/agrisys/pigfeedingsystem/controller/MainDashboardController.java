package dk.agrisys.pigfeedingsystem.controller;

import dk.agrisys.pigfeedingsystem.SessionContext;
import dk.agrisys.pigfeedingsystem.dao.InviteCodeDAO;
import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;
import dk.agrisys.pigfeedingsystem.model.User;
import dk.agrisys.pigfeedingsystem.model.UserRole;
import dk.agrisys.pigfeedingsystem.service.CsvExportService;
import dk.agrisys.pigfeedingsystem.service.ExcelImportService;
import dk.agrisys.pigfeedingsystem.service.FeedingDataService;
import dk.util.IController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.TreeMap;

public class MainDashboardController implements IController {

    @FXML
    private Tab adminTab;

    @FXML
    private Tab dataTab;

    @FXML
    private Tab warningTab;

    @FXML
    private TextFlow dataTextLog;

    @FXML
    private Button exportCSV;

    @FXML
    private Button exportXLSX;

    @FXML
    private Text inviteCodeTextDisplay;

    @FXML
    private Button generateAdminInvite;

    @FXML
    private Button generateInvite;

    @FXML
    private Button importCSV;

    @FXML
    private Button importXLSX;

    @FXML
    private Button copyCodeToClipboard;

    @FXML
    private PieChart pieChart;

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private StackedBarChart<String, Number> stackbarChart;

    @FXML private Tab kpiTab;

    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void initialize() {
        User user = SessionContext.getCurrentUser();
        if (user == null) return;

        UserRole userRole = user.getRole();
        if (userRole == null) return;

        if (userRole == UserRole.SUPERUSER) {
            adminTab.setDisable(false);
        } else {
            adminTab.setDisable(true);
            adminTab.getContent().setStyle("-fx-background-color: grey;");
        }

        pieChart.setLegendVisible(false);
        lineChart.setLegendVisible(false);

        populateLineChart();
        populatePieChart();
        populateStackedBarChart();
    }

    public void importXLSX(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        ExcelImportService eis = new ExcelImportService();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Spreadsheets", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) eis.importFromExcel(selectedFile);
    }

    public void generateUserInvite(ActionEvent e) {
        String code = dk.agrisys.pigfeedingsystem.Generator.generate(16);
        InviteCodeDAO inviteCodeDAO = new InviteCodeDAO();
        try {
            inviteCodeDAO.saveCodeToDb(code, false);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        inviteCodeTextDisplay.setText(code);
    }

    public void generateAdminInvite(ActionEvent e) {
        String code = dk.agrisys.pigfeedingsystem.Generator.generate(16);
        InviteCodeDAO inviteCodeDAO = new InviteCodeDAO();
        try {
            inviteCodeDAO.saveCodeToDb(code, true);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        inviteCodeTextDisplay.setText(code);
    }

    public void handleExportButtonClick(ActionEvent event) {
        CsvExportService csvExportService = new CsvExportService();

        // Fetch data from dbo.pig and dbo.feeding
        List<Pig> pigs = fetchPigs();
        List<FeedingRecord> feedingRecords = fetchFeedingRecords();

        // Predefined file path - ændret til .csv extension
        String filePath = FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/Exports/pig_feeding_data.csv";

        File file = new File(filePath);
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            System.err.println("Error: Failed to create export directory.");
            return;
        }

        boolean success = csvExportService.exportToExcel(pigs, feedingRecords, filePath);
        if (success) {
            System.out.println("Export successful: " + filePath);
        } else {
            System.err.println("Export failed.");
        }

    }


    private List<Pig> fetchPigs() {
        FeedingDataService service = new FeedingDataService();
        return service.getAllPigs();
    }

    private List<FeedingRecord> fetchFeedingRecords() {
        FeedingDataService service = new FeedingDataService();
        return service.getAllFeedingRecords(null, LocalDateTime.of(1,1,1,0,0)); // Default values
    }
    public void handleExportButtonClick() {
        CsvExportService csvExportService = new CsvExportService();

        // Fetch data from dbo.pig and dbo.feeding
        List<Pig> pigs = fetchPigs();
        List<FeedingRecord> feedingRecords = fetchFeedingRecords();

        if (pigs.isEmpty() && feedingRecords.isEmpty()) {
            System.err.println("No data to export.");
            return;
        }
        String filePath = FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/Dokumenter/Exports/pig_feeding_data.xlsx";
        boolean sucess = csvExportService.exportToExcel(pigs, feedingRecords, filePath);

        if (sucess) {
            System.out.println("Export successful: " + filePath);
        } else {
            System.err.println("Export failed.");
        }

    }

    public void copyCodeToClipboard(ActionEvent e){
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(
                        new StringSelection(inviteCodeTextDisplay.getText()),
                        null
                );
    }

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

            // Adjust the Y-axis to avoid forcing zero
            NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
            yAxis.setForceZeroInRange(false);
        } else {
            System.err.println("No feeding records found.");
        }
    }

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


}