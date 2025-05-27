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

/**
 * Controller for the warning list.
 * Handles the display of warnings related to pigs and their feed intake.
 */
public class WarningListController {

    @FXML
    private TableView<Pig> warningTable; // Table for displaying warnings

    @FXML
    private TableColumn<Pig, String> pigIdColumn; // Column for the pig's ID

    @FXML
    private TableColumn<Pig, String> warningTypeColumn; // Column for the type of warning

    @FXML
    private TableColumn<Pig, String> messageColumn; // Column for the warning message

    @FXML
    private TableColumn<Pig, String> createdDateColumn; // Column for the creation date of the warning

    @FXML
    private TableColumn<Pig, Boolean> resolvedColumn; // Column for the resolution status of the warning

    private final WarningService warningService = new WarningService(); // Service for handling warnings

    /**
     * Initializes the controller and sets up the table columns.
     */
    @FXML
    private void initialize() {
        // Configure table columns
        pigIdColumn.setCellValueFactory(new PropertyValueFactory<>("tagNumber")); // Retrieves the pig's ID
        warningTypeColumn.setCellValueFactory(param -> new SimpleStringProperty("Low feed intake")); // Fixed warning type
        messageColumn.setCellValueFactory(param -> new SimpleStringProperty("Feed intake is below the minimum")); // Fixed message
        createdDateColumn.setCellValueFactory(param -> new SimpleStringProperty(java.time.LocalDate.now().toString())); // Today's date
        resolvedColumn.setCellValueFactory(param -> new SimpleBooleanProperty(false)); // Default status: unresolved

        System.out.println("Initializing WarningListController");
        // Load warnings into the table
        loadWarnings();
    }

    /**
     * Loads warnings from the WarningService and displays them in the table.
     */
    private void loadWarnings() {
        System.out.println("Loading warnings...");
        List<Pig> warnings = warningService.checkWarnings(); // Retrieve warnings from the service
        if (warnings != null && !warnings.isEmpty()) {
            ObservableList<Pig> observableWarnings = FXCollections.observableArrayList(warnings); // Convert to ObservableList
            warningTable.setItems(observableWarnings); // Set data in the table
            System.out.println("Warnings loaded: " + warnings.size());
        } else {
            System.out.println("No warnings to display."); // No warnings found
        }
    }
}