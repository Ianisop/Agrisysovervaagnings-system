package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.dao.UserDAO;
import dk.agrisys.pigfeedingsystem.model.User;

/**
 * Service class for handling user authentication.
 * This class interacts with the UserDAO to verify user credentials.
 */
public class AuthenticationService {

    private final UserDAO userDAO; // DAO for accessing user data in the database

    /**
     * Constructor for AuthenticationService.
     * Initializes the UserDAO instance.
     * Note: In a real-world application, dependency injection (e.g., with Spring or Guice) would be used.
     */
    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates a user by verifying the username and password against the database.
     * @param username The username of the user.
     * @param password The password of the user.
     * @return A User object if authentication is successful, otherwise null.
     */
    public User authenticate(String username, String password) {
        User user = userDAO.verifyUserLoginFromDb(username, password); // Verify credentials
        if (user != null) {
            return user; // Return the authenticated user
        }
        System.out.println("Service: Authentication failed for " + username); // Log failure
        return null; // Return null if authentication fails
    }
}