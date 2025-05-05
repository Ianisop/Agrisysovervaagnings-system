package dk.agrisys.pigfeedingsystem.dao;
import dk.agrisys.pigfeedingsystem.SessionContext;
import dk.agrisys.pigfeedingsystem.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class InviteCodeDAO {
    public String generateCode(){
        return UUID.randomUUID().toString();
    }

    public boolean saveCodeToDb(String code) throws SQLException {
        String query = "INSERT INTO Invites (Code,CreatedAt,UsedBy,CreatedBy) VALUES(?,CAST(? AS DATETIME2),NULL,?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (conn == null) {
                System.err.println("DAO (DB): Failed to establish database connection.");
                return false;
            }
            Timestamp timestamp = new Timestamp(System.currentTimeMillis()); // get current time
            User user = SessionContext.getCurrentUser();
            pstmt.setString(1, code);
            pstmt.setTimestamp(2, timestamp);
            pstmt.setString(3, user.getId());


            int rowsAffected = pstmt.executeUpdate();


        }catch(SQLException e) {
            System.out.println("DAO (DB): Failed to save invite code to database: " + e.getMessage());
        }
        {

        }


        return false;
    }
}
