import javafx.application.Application; // Importing Application class from JavaFX
import javafx.fxml.FXMLLoader; // Importing FXMLLoader class from JavaFX
import javafx.scene.Scene; // Importing Scene class from JavaFX
import javafx.scene.control.Alert; // Importing Alert class from JavaFX
import javafx.scene.control.Alert.AlertType; // Importing AlertType enum from JavaFX
import javafx.scene.layout.AnchorPane; // Importing AnchorPane class from JavaFX
import javafx.stage.Stage; // Importing Stage class from JavaFX



public class PigMonitor extends Application {
    private Stage primaryStage; // Primary stage for the application

    @Override
    public void start(Stage primaryStage) { // Overriding the start method
        this.primaryStage = primaryStage; // Assigning the primary stage
        showStartPage(); // Showing the start page
    }



    // Method to show the start page
    public void showStartPage() {
        try {
            FXMLLoader loader = new FXMLLoader(); // Creating a new FXMLLoader instance
            loader.setLocation(getClass().getResource("JavaFX.fxml")); // Setting the location of the FXML file
            AnchorPane mainPane = loader.load(); // Loading the FXML file

            PigMonitorController controller = loader.getController(); // Getting the controller associated with the FXML file
            controller.setMainApp(this); // Setting the main application in the controller

            Scene scene = new Scene(mainPane); // Creating a new scene with the loaded layout
            primaryStage.setScene(scene); // Setting the scene on the primary stage
            primaryStage.show(); // Showing the primary stage
        } catch (Exception e) { // Handling exceptions
            e.printStackTrace(); // Printing the stack trace
        }
    }

    public static void main(String[] args) { // Main method to launch the application
        launch(args); // Launching the application
    }

}
