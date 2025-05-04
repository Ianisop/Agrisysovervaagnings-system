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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class MainDashboardController {
    @FXML // fx:id="adminTab"
    private Tab adminTab; // Value injected by FXMLLoader

    @FXML // fx:id="dataTab"
    private Tab dataTab; // Value injected by FXMLLoader

    @FXML // fx:id="dataTextLog"
    private TextFlow dataTextLog; // Value injected by FXMLLoader

    @FXML // fx:id="exportCSV"
    private Button exportCSV; // Value injected by FXMLLoader

    @FXML // fx:id="exportXLSX"
    private Button exportXLSX; // Value injected by FXMLLoader

    @FXML // fx:id="generateAdminInvite"
    private Button generateAdminInvite; // Value injected by FXMLLoader

    @FXML // fx:id="generateInvite"
    private Button generateInvite; // Value injected by FXMLLoader

    @FXML // fx:id="importCSV"
    private Button importCSV; // Value injected by FXMLLoader

    @FXML // fx:id="importXLSX"
    private Button importXLSX; // Value injected by FXMLLoader

    @FXML // fx:id="kpiTab"
    private Tab kpiTab; // Value injected by FXMLLoader

    @FXML
    private void initialize() {

    }








}
