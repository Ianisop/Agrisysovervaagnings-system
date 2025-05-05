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
 * Controller for login view.
 * Håndterer bruger-login og visning af dashboard eller fejlmeddelelser.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final AuthenticationService authService;

    /**
     * Constructor som initialiserer AuthenticationService.
     */
    public LoginController() {
        this.authService = new AuthenticationService();
    }

    /**
     * Initialiseringsmetode til at nulstille fejltekst.
     */
    @FXML
    private void initialize() {
        errorLabel.setText("");
    }

    /**
     * Håndterer login-knap. Validerer bruger og skifter til dashboard ved succes.
     */
    @FXML
    private void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (isEmpty(username) || isEmpty(password)) {
            errorLabel.setText("Begge felter skal udfyldes.");
            return;
        }

        User authenticatedUser = authService.authenticate(username, password);

        if (authenticatedUser != null) {
            errorLabel.setText("");
            System.out.println("Login succesfuld for: " + authenticatedUser.getUsername()
                    + " | Rolle: " + authenticatedUser.getRole());

            try {
                // Gem bruger globalt hvis nødvendigt: App.setCurrentUser(authenticatedUser);
                SessionContext.setCurrentUser(authenticatedUser);
                App.loadScene("view/MainDashboardView.fxml");
            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("Fejl: Kunne ikke loade dashboard.");
            }
        } else {
            errorLabel.setText("Ugyldigt brugernavn eller password.");
        }
    }

    /**
     * Håndterer "Create User"-knap og åbner view til oprettelse.
     */
    @FXML
    private void handleCreateUserButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CreateUserView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Opret Bruger");
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Fejl: Kunne ikke loade Opret Bruger-view.");
        }
    }

    /**
     * Hjælpefunktion til at kontrollere tomme felter.
     * @param value Tekstfeltets værdi
     * @return true hvis tom eller null
     */
    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
