package dk.agrisys.pigfeedingsystem.controller;

import dk.App;
import dk.agrisys.pigfeedingsystem.SessionContext;
import dk.agrisys.pigfeedingsystem.model.User;
import dk.agrisys.pigfeedingsystem.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the login view.
 * Handles user login and navigation to the dashboard or displays error messages.
 */
public class LoginController {

    @FXML
    private TextField usernameField; // Field for entering the username

    @FXML
    private PasswordField passwordField; // Field for entering the password

    @FXML
    private Label errorLabel; // Label for displaying error messages

    private final AuthenticationService authService; // Service for handling authentication

    /**
     * Constructor that initializes the AuthenticationService.
     */
    public LoginController() {
        this.authService = new AuthenticationService();
    }

    /**
     * Initialization method that resets the error message.
     */
    @FXML
    private void initialize() {
        errorLabel.setText(""); // Reset the error message
    }

    /**
     * Handles the login button action.
     * Validates user credentials and navigates to the dashboard on success.
     */
    @FXML
    private void handleLoginButtonAction() {
        String username = usernameField.getText(); // Retrieve the username from the field
        String password = passwordField.getText(); // Retrieve the password from the field

        // Check if the fields are empty
        if (isEmpty(username) || isEmpty(password)) {
            errorLabel.setText("Both fields must be filled."); // Display error message
            return;
        }

        // Attempt to authenticate the user
        User authenticatedUser = authService.authenticate(username, password);

        if (authenticatedUser != null) {
            // Login was successful
            errorLabel.setText(""); // Reset the error message
            System.out.println("Login successful for: " + authenticatedUser.getUsername()
                    + " | Role: " + authenticatedUser.getRole());

            try {
                // Save the authenticated user in the session
                SessionContext.setCurrentUser(authenticatedUser);
                // Switch to the dashboard view
                App.loadScene("view/MainDashboardView.fxml");
            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("Error: Could not load the dashboard."); // Display error message
            }
        } else {
            // Login failed
            errorLabel.setText("Invalid username or password."); // Display error message
        }
    }

    /**
     * Handles the "Create User" button action and opens the user creation view.
     */
    @FXML
    private void handleCreateUserButtonAction() {
        try {
            // Load CreateUserView.fxml and switch the scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CreateUserView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Create User");
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Error: Could not load the Create User view."); // Display error message
        }
    }

    /**
     * Helper function to check if a text field is empty.
     * @param value The value of the text field
     * @return true if the field is empty or null
     */
    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}