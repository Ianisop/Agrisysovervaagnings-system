package dk.agrisys.pigfeedingsystem.dao;

import dk.agrisys.pigfeedingsystem.model.User;
import dk.agrisys.pigfeedingsystem.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * DAO class for handling user data.
 * Contains methods to register, validate, and retrieve users from the database.
 */
public class UserDAO {

    private User user; // User object to hold data about a specific user

    /**
     * Registers a new user in the database.
     * @param username The username of the new user.
     * @param plainPassword The user's password in plain text.
     * @param role The user's role (USER or SUPERUSER).
     * @param userID The unique ID of the user.
     * @param inviteCode The invitation code used for registration.
     * @return True if the registration was successful, otherwise false.
     */
    public boolean registerUserInDb(String username, String plainPassword, UserRole role, int userID, String inviteCode) {
        if (username == null || username.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty() || role == null || inviteCode == null || inviteCode.isEmpty()) {
            System.err.println("DAO (DB): Invalid input to registerUserInDb.");
            return false;
        }

        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt()); // Hash the password
        int roleId = (role == UserRole.SUPERUSER) ? 2 : 1; // Assign role ID based on the user's role

        String insertUserSQL = "INSERT INTO [User] (Username, PasswordHash, RoleID, UserID) VALUES (?, ?, ?, ?)";
        String updateInviteSQL = "UPDATE Invites SET UsedBy = ? WHERE Code = ?";

        try (Connection conn = DatabaseConnector.getConnection()) {
            if (conn == null) {
                System.err.println("DAO (DB): Could not establish a connection to the database.");
                return false;
            }

            conn.setAutoCommit(false); // Start transaction

            try (
                PreparedStatement insertUserStmt = conn.prepareStatement(insertUserSQL);
                PreparedStatement updateInviteStmt = conn.prepareStatement(updateInviteSQL)
            ) {
                // Insert new user
                insertUserStmt.setString(1, username.trim());
                insertUserStmt.setString(2, hashedPassword);
                insertUserStmt.setInt(3, roleId);
                insertUserStmt.setInt(4, userID);
                int userInsertResult = insertUserStmt.executeUpdate();

                if (userInsertResult == 0) {
                    conn.rollback();
                    System.err.println("DAO (DB): Could not insert user.");
                    return false;
                }

                // Update invitation code
                updateInviteStmt.setInt(1, userID);
                updateInviteStmt.setString(2, inviteCode);
                int inviteUpdateResult = updateInviteStmt.executeUpdate();

                if (inviteUpdateResult == 0) {
                    conn.rollback();
                    System.err.println("DAO (DB): Invalid or already used invitation code.");
                    return false;
                }

                conn.commit();
                System.out.println("DAO (DB): User registered and invitation marked as used.");
                return true;
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                throw e;
            } finally {
                conn.setAutoCommit(true); // Restore autocommit
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 2627 || e.getErrorCode() == 2601) {
                System.err.println("DAO (DB): Username '" + username + "' already exists.");
            } else {
                System.err.println("DAO (DB): SQL error: " + e.getMessage());
            }
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("DAO (DB): Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifies the user's login credentials.
     * @param username The username.
     * @param plainPassword The password in plain text.
     * @return A User object if login is successful, otherwise null.
     */
    public User verifyUserLoginFromDb(String username, String plainPassword) {
        String sql = "SELECT UserID, Username, PasswordHash, RoleID FROM [User] WHERE Username = ?";
        User user = null;

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("DAO (DB): Could not establish a connection to the database for login verification.");
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

                    user = new User(username, null, role, String.valueOf(userId));
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

    /**
     * Retrieves a user based on the username.
     * @param username The username.
     * @return A User object if the user exists, otherwise null.
     */
    public User getUser(String username) {
        String sql = "SELECT Username FROM [User] WHERE Username = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(rs.getString(1), null, null, null);
                System.out.println("Found user with name: " + username);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("DAO (DB): SQL error while retrieving user '" + username + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * Validates if a user with a specific ID exists in the database.
     * @param id The user's ID.
     * @return True if the user exists, otherwise false.
     */
    public boolean validateUserID(int id) {
        String sql = "SELECT * FROM [User] WHERE UserID = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("DAO (DB): SQL error while validating user ID '" + id + "': " + e.getMessage());
            e.printStackTrace();

            return false;
        }
        return true;
    }
}