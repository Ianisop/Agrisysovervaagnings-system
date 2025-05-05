package dk.util;

import dk.agrisys.pigfeedingsystem.dao.UserDAO;
import javafx.stage.Stage;

@FunctionalInterface
public interface IController  {
    void setPrimaryStage(Stage stage);
}
