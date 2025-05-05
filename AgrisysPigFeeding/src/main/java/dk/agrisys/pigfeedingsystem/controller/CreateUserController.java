package dk.agrisys.pigfeedingsystem.controller;

import dk.agrisys.pigfeedingsystem.model.UserRole;
import dk.agrisys.pigfeedingsystem.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for creating new users (SUPERUSER by default).
 */
public class CreateUserController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField inviteCodeField;

    @FXML
    private Label statusLabel;

    private final UserService userService;

    /**
     * Constructor initializes the UserService.
     */
    public CreateUserController() {
        this.userService = new UserService();
    }

    /**
     * Handles the Create User button click.
     * Validates input and attempts to create a SUPERUSER.
     */
   @FXML
    private void handleCreateUserAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String inviteCode = inviteCodeField.getText();

        // Input validation
        if (isEmpty(username) || isEmpty(password)) {
            showStatus("All fields are required.", "red");
            return;
        }

        try {
            boolean success = userService.createUser(username, password, inviteCode); // Create regular USER
            if (success) {
                showStatus("User created successfully!", "green");
                clearFields();
            } else {
                showStatus("Failed to create user (already exists?).", "red");
            }
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), "red");
            e.printStackTrace(); // For debugging
        }
    }

    /**
     * Checks if a string is null or empty.
     * @param input the string to check
     * @return true if null or empty
     */
    private boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    /**
     * Clears the input fields.
     */
    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }

    /**
     * Displays status message to the user.
     * @param message The message text
     * @param color "red" or "green"
     */
    private void showStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + color + ";");
    }
}
