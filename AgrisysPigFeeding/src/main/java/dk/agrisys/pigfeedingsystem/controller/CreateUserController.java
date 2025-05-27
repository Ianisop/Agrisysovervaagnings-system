package dk.agrisys.pigfeedingsystem.controller;

import dk.App;
import dk.agrisys.pigfeedingsystem.model.UserRole;
import dk.agrisys.pigfeedingsystem.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for creating new users (default role: SUPERUSER).
 */
public class CreateUserController {

    @FXML
    private TextField usernameField; // Field for entering the username

    @FXML
    private TextField passwordField; // Field for entering the password

    @FXML
    private TextField inviteCodeField; // Field for entering the invite code

    @FXML
    private Label statusLabel; // Label for displaying status messages

    private final UserService userService; // Service for managing user operations

    /**
     * Constructor that initializes the UserService.
     */
    public CreateUserController() {
        this.userService = new UserService();
    }

    /**
     * Handles the "Create User" button click.
     * Validates input and attempts to create a SUPERUSER.
     */
    @FXML
    private void handleCreateUserAction() {
        String username = usernameField.getText(); // Retrieve the username from the field
        String password = passwordField.getText(); // Retrieve the password from the field
        String inviteCode = inviteCodeField.getText(); // Retrieve the invite code from the field

        // Validate input
        if (isEmpty(username) || isEmpty(password)) {
            showStatus("All fields must be filled.", "red");
            return;
        }

        try {
            // Attempt to create a new user
            boolean success = userService.createUser(username, password, inviteCode);
            if (success) {
                showStatus("User created successfully!", "green");
                clearFields(); // Clear the input fields
                App.loadScene("view/LoginView.fxml"); // Switch to the login view
            } else {
                showStatus("Could not create user (already exists?).", "red");
            }
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), "red");
            e.printStackTrace(); // Print the error for debugging
        }
    }

    /**
     * Checks if a string is empty or null.
     * @param input The string to check
     * @return true if the string is empty or null
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
     * Displays a status message to the user.
     * @param message The text of the message
     * @param color "red" or "green" for the message color
     */
    private void showStatus(String message, String color) {
        statusLabel.setText(message); // Set the message text
        statusLabel.setStyle("-fx-text-fill: " + color + ";"); // Set the text color
    }
}