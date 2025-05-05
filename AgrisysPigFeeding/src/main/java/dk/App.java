package dk;

import com.sun.tools.javac.Main;
import dk.agrisys.pigfeedingsystem.controller.MainDashboardController;
import dk.agrisys.pigfeedingsystem.dao.DatabaseConnector;
import dk.agrisys.pigfeedingsystem.dao.FeedingRecordDAO;
import dk.agrisys.pigfeedingsystem.dao.InviteCodeDAO;
import dk.agrisys.pigfeedingsystem.dao.UserDAO;
import dk.agrisys.pigfeedingsystem.model.User;
import dk.agrisys.pigfeedingsystem.service.ExcelImportService;
import dk.agrisys.pigfeedingsystem.service.UserService;
import dk.util.IController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.poi.ss.formula.functions.T;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Test database connection
        System.out.println("Testing database driver...");
        DatabaseConnector.testConnection();
        System.out.println("Database driver test completed.");
        InviteCodeDAO invCodeDao = new InviteCodeDAO();
        invCodeDao.createInitialCode();
        invCodeDao.createInitialUser();
        System.out.println("INITIAL CODE && USER CREATED!");
        // Start with the login view
        loadScene("view/LoginView.fxml");

        stage.setTitle("Agrisys Pig Feeding System");
        stage.show();
    }

    // Method to switch scenes
    public static void loadScene(String fxmlFile) throws IOException {
        // Ensure the path is relative to the 'resources' folder
        if (!fxmlFile.startsWith("/")) {
            fxmlFile = "/" + fxmlFile.replace("dk/agrisys/", "");
        }

        FXMLLoader loader = new FXMLLoader();

        URL fxmlUrl = App.class.getResource(fxmlFile);
        if (fxmlUrl == null) {
            System.err.println("Error: Could not find FXML file: " + fxmlFile);
            throw new IOException("Cannot load FXML file: " + fxmlFile);
        }
        System.out.println("Loading FXML: " + fxmlUrl);

        Parent root = loader.load(fxmlUrl);
        Scene scene = new Scene(root);

        // Apply CSS if the file exists
        URL cssUrl = App.class.getResource("/css/styles.css");
        if (cssUrl != null) {
            System.out.println("Loading CSS: " + cssUrl);
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("CSS file /css/styles.css not found.");
        }
        Object controller = loader.getController(); // has to fetch this in order to pass the stage
        if (controller instanceof IController) {
            ((IController) controller).setPrimaryStage(primaryStage); // cooked hard
        }


        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }


}