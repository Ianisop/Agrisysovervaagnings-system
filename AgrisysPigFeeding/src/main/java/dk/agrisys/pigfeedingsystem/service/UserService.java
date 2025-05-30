package dk.agrisys.pigfeedingsystem.service;

import dk.util.Generator;
import dk.agrisys.pigfeedingsystem.dao.InviteCodeDAO;
import dk.agrisys.pigfeedingsystem.dao.UserDAO;
import dk.agrisys.pigfeedingsystem.model.UserRole;

/**
 * Service class for managing user-related operations.
 * Provides methods to create users and interact with the UserDAO.
 */
public class UserService {

    private final UserDAO userDao; // DAO for accessing user data in the database

    /**
     * Constructor for UserService.
     * Initializes the UserDAO instance.
     */
    public UserService() {
        this.userDao = new UserDAO();
    }

    /**
     * Creates a new user in the system.
     * Validates the input, checks if the user already exists, and assigns a role based on the invite code.
     * @param username The username of the new user.
     * @param password The password of the new user.
     * @param inviteCode The invite code used to determine the user's role.
     * @return True if the user was successfully created, false otherwise.
     */
    public boolean createUser(String username, String password, String inviteCode) {
        // Validate input and check if the user already exists
        if (username == null || username.isEmpty() || password == null || password.isEmpty() || userDao.getUser(username) != null) {
            System.out.println("USER ALREADY EXISTS");
            return false; // Validation failed
        }

        // Determine the user's role based on the invite code
        InviteCodeDAO ico = new InviteCodeDAO();
        UserRole role = ico.getInviteCodeRoleType(inviteCode) ? UserRole.SUPERUSER : UserRole.USER;

        // Generate a unique ID for the user and save the user via DAO
        return userDao.registerUserInDb(username, password, role, Integer.parseInt(Generator.generate(8)), inviteCode);
    }

    /**
     * Retrieves the UserDAO instance.
     * @return The UserDAO instance used by this service.
     */
    public UserDAO getDAO() {
        return userDao;
    }
}