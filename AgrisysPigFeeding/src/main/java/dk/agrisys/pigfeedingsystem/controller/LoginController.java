package dk.agrisys.pigfeedingsystem.controller;

import dk.App; // Antager din main app hedder App
import dk.agrisys.pigfeedingsystem.model.User;
import dk.agrisys.pigfeedingsystem.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private AuthenticationService authService;

    public LoginController() {
        this.authService = new AuthenticationService();
    }

    @FXML
    private void initialize() {
        errorLabel.setText(""); // Skjul fejlmeddelelse ved start
    }

    @FXML
    private void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User authenticatedUser = authService.authenticate(username, password);

        if (authenticatedUser != null) {
            errorLabel.setText("");
            System.out.println("Login succesfuld for: " + authenticatedUser.getUsername() + " Rolle: " + authenticatedUser.getRole());
            // Skift til hoved-dashboardet
            try {
                // Gem bruger info et centralt sted hvis nødvendigt
                // App.setCurrentUser(authenticatedUser); // Kræver metode i App.java
                App.loadScene("view/MainDashboardView.fxml"); // Sørg for stien er korrekt
            } catch (IOException e) {
                errorLabel.setText("Fejl: Kunne ikke loade dashboard.");
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Ugyldigt brugernavn eller password.");
        }
    }
}