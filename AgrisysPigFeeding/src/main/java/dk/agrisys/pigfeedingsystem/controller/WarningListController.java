package dk.agrisys.pigfeedingsystem.controller;

import dk.agrisys.pigfeedingsystem.model.Pig;
import dk.agrisys.pigfeedingsystem.service.WarningService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class WarningListController {

    @FXML
    private TableView<Pig> warningTable;

    @FXML
    private TableColumn<Pig, String> pigIdColumn;

    @FXML
    private TableColumn<Pig, String> warningTypeColumn;

    @FXML
    private TableColumn<Pig, String> BeskedColumn;

    @FXML
    private TableColumn<Pig, String> OprettedColumn;

    @FXML
    private TableColumn<Pig, Boolean> LøstColumn;

    private final WarningService warningService = new WarningService();

    @FXML
    private void initialize() {
        // Set up table columns
        pigIdColumn.setCellValueFactory(new PropertyValueFactory<>("tagNumber"));
        warningTypeColumn.setCellValueFactory(param -> new SimpleStringProperty("Lav foderindtag"));
        BeskedColumn.setCellValueFactory(param -> new SimpleStringProperty("fodringintag er under minimum"));
        OprettedColumn.setCellValueFactory(param -> new SimpleStringProperty(java.time.LocalDate.now().toString()));
        LøstColumn.setCellValueFactory(param -> new SimpleBooleanProperty(false));

        System.out.println("initializing WariningListController");
        // Load warnings into the table
        loadWarnings();
    }

    private void loadWarnings() {
        System.out.println("Loading warnings...");
        List<Pig> warnings = warningService.checkWarnings();
        if (warnings != null && !warnings.isEmpty()) {
            ObservableList<Pig> observableWarnings = FXCollections.observableArrayList(warnings);
            warningTable.setItems(observableWarnings);
            System.out.println("Warnings loaded: " + warnings.size());
        } else {
            System.out.println("No warnings to display.");
        }
    }
}