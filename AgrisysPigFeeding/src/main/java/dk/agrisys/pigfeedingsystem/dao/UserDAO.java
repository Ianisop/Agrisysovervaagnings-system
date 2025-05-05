package dk.agrisys.pigfeedingsystem.dao;
import dk.agrisys.pigfeedingsystem.Generator;
import dk.agrisys.pigfeedingsystem.model.User;
import dk.agrisys.pigfeedingsystem.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class UserDAO {

    // --- Mock In-Memory "Database" ---
    private static final Map<String, User> mockUsers = new HashMap<>();
    private User user;

    static {
        mockUsers.put("super", new User("super", "pass1", UserRole.SUPERUSER));
        mockUsers.put("user", new User("user", "pass2", UserRole.USER));
        System.out.println("DAO: Mock users initialized (INSECURE).");
    }

    // Mock method to find a user by username
    public User findUserByUsername(String username) {
        return mockUsers.get(username);
    }

    // Mock method to create a user
    public boolean createUser(String username, String password, UserRole role, int id) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty() || role == null) {
            System.err.println("DAO (Mock): Invalid input for createUser.");
            return false;
        }

        User user = new User(username, password, role);
        return mockUsers.putIfAbsent(username, user) == null;
    }

    // === Method: Register a new user in the database ===
    public boolean registerUserInDb(String username, String plainPassword, UserRole role, int userID) {
        if (username == null || username.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty() || role == null) {
            System.err.println("DAO (DB): Invalid input for registerUserInDb.");
            return false;
        }

        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        int roleId = (role == UserRole.SUPERUSER) ? 2 : 1;

        String sql = "INSERT INTO [User] (Username, PasswordHash, RoleID,UserID) VALUES (?,?, ?, ?)"; // add invite code column and value

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("DAO (DB): Failed to establish database connection.");
                return false;
            }

            pstmt.setString(1, username.trim());
            pstmt.setString(2, hashedPassword);
            pstmt.setInt(3, roleId);
            pstmt.setInt(4, userID);


            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("DAO (DB): User '" + username + "' successfully registered.");
                return true;
            } else {
                System.err.println("DAO (DB): No rows affected for '" + username + "'.");
                return false;
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                System.err.println("DAO (DB): Username '" + username + "' already exists.");
            } else {
                System.err.println("DAO (DB): SQL error during registration of '" + username + "': " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        } catch (Exception e) {
            System.err.println("DAO (DB): Unexpected error during registration of '" + username + "': " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // === Method: Verify user login credentials ===
    public User verifyUserLoginFromDb(String username, String plainPassword) {
        String sql = "SELECT UserID, Username, PasswordHash, RoleID FROM [User] WHERE Username = ?";
        User user = null;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("DAO (DB): Failed to establish database connection for login verification.");
                return null;
            }

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("PasswordHash");
                int userId = rs.getInt("UserID");

                if (BCrypt.checkpw(plainPassword, storedHash)) {
                    int roleId = rs.getInt("RoleID");
                    UserRole role = (roleId == 2) ? UserRole.SUPERUSER : UserRole.USER;

                    user = new User(username, null, role);
                    System.out.println("DAO (DB): Login successful for: " + username);
                } else {
                    System.out.println("DAO (DB): Incorrect password for: " + username);
                }
            } else {
                System.out.println("DAO (DB): User not found: " + username);
            }

        } catch (SQLException e) {
            System.err.println("DAO (DB): SQL error during login verification for '" + username + "': " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("DAO (DB): Unexpected error during login verification for '" + username + "': " + e.getMessage());
            e.printStackTrace();
        }
        return user;
    }

    public User getUser(String username)
    {
        String sql = "SELECT RoleID FROM [User] WHERE Username = ?";
        User user = null;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
        } catch (SQLException e) {
        System.err.println("DAO (DB): SQL error during login verification for '" + username + "': " + e.getMessage());
        e.printStackTrace();

        return user;
        }
        return user;
    }
    public boolean validateUserID(int id)
    {
        String sql = "SELECT UserID FROM [User] WHERE UserID = ?";
        User user = null;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("DAO (DB): SQL error during login verification for '" + id + "': " + e.getMessage());
            e.printStackTrace();

            return false;
        }
        return true;
    }
}