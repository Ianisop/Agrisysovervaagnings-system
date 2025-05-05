package dk.agrisys.pigfeedingsystem.controller;

import dk.App;
import dk.agrisys.pigfeedingsystem.Generator;
import dk.agrisys.pigfeedingsystem.SessionContext;
import dk.agrisys.pigfeedingsystem.dao.InviteCodeDAO;
import dk.agrisys.pigfeedingsystem.dao.UserDAO;
import dk.agrisys.pigfeedingsystem.model.FeedingRecord;
import dk.agrisys.pigfeedingsystem.model.Pig;
import dk.agrisys.pigfeedingsystem.model.User;
import dk.agrisys.pigfeedingsystem.model.UserRole;
import dk.agrisys.pigfeedingsystem.service.*;
import dk.util.IController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class MainDashboardController implements IController {
    @FXML // fx:id="adminTab"
    private Tab adminTab; // Value injected by FXMLLoader

    @FXML // fx:id="dataTab"
    private Tab dataTab; // Value injected by FXMLLoader

    @FXML // fx:id="dataTextLog"
    private TextFlow dataTextLog; // Value injected by FXMLLoader

    @FXML // fx:id="exportCSV"
    private Button exportCSV; // Value injected by FXMLLoader

    @FXML // fx:id="exportXLSX"
    private Button exportXLSX; // Value injected by FXMLLoader

    @FXML // fx:id="generateAdminInvite"
    private Button generateAdminInvite; // Value injected by FXMLLoader

    @FXML // fx:id="generateInvite"
    private Button generateInvite; // Value injected by FXMLLoader

    @FXML // fx:id="importCSV"
    private Button importCSV; // Value injected by FXMLLoader

    @FXML // fx:id="importXLSX"
    private Button importXLSX; // Value injected by FXMLLoader

    @FXML // fx:id="kpiTab"
    private Tab kpiTab; // Value injected by FXMLLoader

    private Stage primaryStage;



    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void initialize() {
        UserService userService = new UserService();
        UserDAO userDAO = userService.getDAO();
        User user = SessionContext.getCurrentUser();
        System.out.println("GETTING USER: " + user);
        if(user == null) return; // return if user doesnt exist
        UserRole userRole = user.getRole();
        System.out.println("USER ROLE IS: " + userRole);
        //UserRole userRole = UserRole.USER;
        if(userRole == null) return;


        if(userRole == UserRole.SUPERUSER) {
            adminTab.setDisable(false); // check if its an admin

        }
        else{
            adminTab.setDisable(true); // check if its an admin
            adminTab.getContent().setStyle("-fx-background-color: grey;");
        }


    }

    //Method to import data into the db using an excel file on click
    public void importXLSX(ActionEvent e)
    {
        FileChooser fileChooser = new FileChooser();
        ExcelImportService eis = new ExcelImportService();
        // filter out xlsx files
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel Spreadsheets", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if(selectedFile != null)  eis.importFromExcel(selectedFile); // import from Excel if the user chooses a file

    }


    public void generateUserInvite(ActionEvent e)  {
        String code = Generator.generate(16);
        InviteCodeDAO inviteCodeDAO = new InviteCodeDAO();
        try {
            inviteCodeDAO.saveCodeToDb(code, false);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void generateAdminInvite(ActionEvent e)
    {
        String code = Generator.generate(16);
        InviteCodeDAO inviteCodeDAO = new InviteCodeDAO();
        try {
            inviteCodeDAO.saveCodeToDb(code, true);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

    }










}
