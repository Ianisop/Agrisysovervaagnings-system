import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.sql.SQLException;

public class PigMonitorController {

    private PigMonitor mainApp; // Reference to the main application


    // Method to set the main application
    public void setMainApp(PigMonitor mainApp) {
        this.mainApp = mainApp;
    }
}
