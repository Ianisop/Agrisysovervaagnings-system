package dk.agrisys.pigfeedingsystem.dao;
import dk.agrisys.pigfeedingsystem.SessionContext;
import dk.agrisys.pigfeedingsystem.model.User;

import java.sql.*;
import java.util.UUID;

public class InviteCodeDAO {
    public String generateCode(){
        return UUID.randomUUID().toString();
    }

    public boolean saveCodeToDb(String code) throws SQLException {
        UserDAO userDAO = new UserDAO();
        boolean userExists = userDAO.validateUserID(Integer.parseInt(SessionContext.getCurrentUser().getId()));
        if(!userExists) System.out.println("USER ID DOESNTE XIST!!!!!!!!!!!");
        String query = "INSERT INTO Invites (Code,CreatedAt,UsedBy,CreatedBy) VALUES(?,CAST(? AS DATETIME2),NULL,?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (conn == null) {
                System.err.println("DAO (DB): Failed to establish database connection.");
                return false;
            }
            Timestamp timestamp = new Timestamp(System.currentTimeMillis()); // get current time
            User user = SessionContext.getCurrentUser();
            pstmt.setLong(1, Long.parseLong(code));
            pstmt.setTimestamp(2, timestamp);
            pstmt.setInt(3, Integer.parseInt(user.getId()));


            int rowsAffected = pstmt.executeUpdate();


        }catch(SQLException e) {
            System.out.println("DAO (DB): Failed to save invite code to database: " + e.getMessage());
        }
        {

        }


        return false;
    }

    //creates intiail admin code
    public void createInitialCode()
    {
        String query = "INSERT INTO Invites (Code,isAdmin,CreatedAt,UsedBy,CreatedBy) VALUES(?,?,CAST(? AS DATETIME2),NULL,NULL)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (conn == null) {
                System.err.println("DAO (DB): Failed to establish database connection.");

            }
            Timestamp timestamp = new Timestamp(System.currentTimeMillis()); // get current time
            User user = SessionContext.getCurrentUser();
            pstmt.setLong(1, Long.parseLong("0123456789")); // initial admin code is 0123456789
            pstmt.setBoolean(2, true); // initial code is admin
            pstmt.setTimestamp(3, timestamp);



            int rowsAffected = pstmt.executeUpdate();


        }catch(SQLException e) {
            System.out.println("DAO (DB): Failed to save invite code to database: " + e.getMessage());
        }

    }


    public boolean getInviteCodeRoleType(String code)
    {
        String query = "SELECT isAdmin FROM Invites WHERE Code = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (conn == null) {
                System.err.println("DAO (DB): Failed to establish database connection.");

            }
            pstmt.setLong(1, Long.parseLong(code));
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return rs.getBoolean("isAdmin");
            }




            int rowsAffected = pstmt.executeUpdate();


        }catch(SQLException e) {
            System.out.println("DAO (DB): Failed to save invite code to database: " + e.getMessage());
        }
        return false;
    }
}
