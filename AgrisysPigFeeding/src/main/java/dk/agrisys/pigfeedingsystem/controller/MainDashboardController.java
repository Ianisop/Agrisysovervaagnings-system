package dk.agrisys.pigfeedingsystem.controller;

import dk.App;
import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;
import dk.agrisys.pigfeedingsystem.service.CsvExportService;
import dk.agrisys.pigfeedingsystem.service.ExcelImportService;
import dk.agrisys.pigfeedingsystem.service.FeedingDataService;
import dk.agrisys.pigfeedingsystem.service.WarningService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class MainDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private ListView<Pig> pigListView; // Simpel liste over grise
    @FXML private TableView<FeedingRecord> feedingTableView; // Tabel til fodringer
    @FXML private TableColumn<FeedingRecord, LocalDateTime> timestampColumn;
    @FXML private TableColumn<FeedingRecord, Double> amountColumn;
    @FXML private Label warningLabel; // Til at vise advarselsstatus

    private FeedingDataService feedingDataService;
    private WarningService warningService;
    private ExcelImportService excelImportService;
    private CsvExportService csvExportService;

    private ObservableList<Pig> pigList = FXCollections.observableArrayList();
    private ObservableList<FeedingRecord> feedingList = FXCollections.observableArrayList();

    public MainDashboardController() {
        this.feedingDataService = new FeedingDataService();
        this.warningService = new WarningService();
        this.excelImportService = new ExcelImportService();
        this.csvExportService = new CsvExportService();
    }

    @FXML
    private void initialize() {
        // Opsætning af TableView kolonner
        // "timestamp" og "amountKg" skal matche getter-navne i FeedingRecord (getTimestamp, getAmountKg)
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amountKg"));

        // Sæt data kilder
        pigListView.setItems(pigList);
        feedingTableView.setItems(feedingList);

        // Lyt efter valg i grise-listen for at opdatere fodrings-tabellen
        pigListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadFeedingDataForPig(newSelection);
            } else {
                feedingList.clear();
            }
        });


        // Indlæs startdata
        loadInitialData();
        checkForWarnings(); // Tjek for advarsler ved start

        // Sæt velkomstbesked (kunne hente brugernavn fra App.currentUser)
        welcomeLabel.setText("Velkommen til Grise Fodringssystem");
    }

    private void loadInitialData() {
        pigList.setAll(feedingDataService.getAllPigs());
        // Vælg den første gris i listen, hvis der er nogen
        if (!pigList.isEmpty()) {
            pigListView.getSelectionModel().selectFirst();
        } else {
            feedingList.clear(); // Ingen grise, ingen fodringer at vise
        }
    }

    private void loadFeedingDataForPig(Pig selectedPig) {
        List<FeedingRecord> records = feedingDataService.getFeedingsForPig(selectedPig);
        feedingList.setAll(records);
        System.out.println("Viste fodringer for: " + selectedPig.getTagNumber());
    }


    @FXML
    private void handleImportExcelAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Vælg Excel fil til import");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Filer", "*.xlsx", "*.xls"));
        File selectedFile = fileChooser.showOpenDialog(welcomeLabel.getScene().getWindow()); // Få fat i scenens vindue

        if (selectedFile != null) {
            boolean success = excelImportService.importFromExcel(selectedFile);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Import Succes", "Excel data blev (simuleret) importeret.");
                // Genindlæs data efter import hvis nødvendigt
                loadInitialData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Import Fejl", "Kunne ikke importere data fra Excel filen.");
            }
        }
    }

    @FXML
    private void handleExportCsvAction() {
        // Eksporter f.eks. den viste liste af fodringer
        if (feedingList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Export Fejl", "Ingen fodringsdata at eksportere.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Gem data som CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Filer", "*.csv"));
        File selectedFile = fileChooser.showSaveDialog(welcomeLabel.getScene().getWindow());

        if (selectedFile != null) {
            // Sørg for at filen har .csv endelse hvis brugeren glemmer det
            if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
            }

            boolean success = csvExportService.exportToCsv(feedingList, selectedFile); // Eksporter den viste liste
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Eksport Succes", "Data blev (simuleret) eksporteret til CSV.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Eksport Fejl", "Kunne ikke eksportere data til CSV filen.");
            }
        }
    }

    @FXML
    private void handleShowWarningsAction() {
        checkForWarnings(); // Kør tjekket igen
        List<Pig> pigsWithWarnings = warningService.checkWarnings(); // Hent resultatet

        if (pigsWithWarnings.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION,"Advarsler", "Ingen grise med lavt foderindtag fundet.");
        } else {
            // Byg en streng med grisene
            StringBuilder warningMessage = new StringBuilder("Følgende grise har lavt foderindtag:\n");
            for(Pig p : pigsWithWarnings) {
                warningMessage.append("- Gris #").append(p.getTagNumber()).append("\n");
            }
            // Vis i en alert dialog
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advarsler Fundet");
            alert.setHeaderText("Grise med lavt foderindtag");
            alert.setContentText(warningMessage.toString());
            alert.showAndWait();
        }
    }

    // Tjekker og opdaterer label
    private void checkForWarnings() {
        List<Pig> warnings = warningService.checkWarnings();
        if (warnings.isEmpty()) {
            warningLabel.setText("Status: Alle grise spiser normalt.");
            warningLabel.setStyle("-fx-text-fill: green;");
        } else {
            warningLabel.setText("Status: " + warnings.size() + " gris(e) har lavt foderindtag!");
            warningLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // Hjælpefunktion til at vise pop-up beskeder
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // Ingen header
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Metode til at logge ud (skal kaldes fra en knap i FXML)
    @FXML
    private void handleLogoutAction() {
        System.out.println("Logger ud...");
        try {
            App.loadScene("view/LoginView.fxml"); // Gå tilbage til login skærmen
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Logout Fejl", "Kunne ikke gå tilbage til login skærmen.");
        }
    }
}