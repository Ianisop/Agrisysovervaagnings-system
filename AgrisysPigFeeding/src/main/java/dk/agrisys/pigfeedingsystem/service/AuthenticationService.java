package dk.agrisys.pigfeedingsystem.service;

import dk.agrisys.pigfeedingsystem.dao.UserDAO;
import dk.agrisys.pigfeedingsystem.model.User;

// (Hvem har skrevet: [Dit Navn/Gruppens Navn])
public class AuthenticationService {

    private final UserDAO userDAO;

    public AuthenticationService() {
        // I et rigtigt system ville DAO blive "injected" (f.eks. med Spring eller Guice)
        this.userDAO = new UserDAO();
    }

    public User authenticate(String username, String password) {
        User user = userDAO.findUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) { // Simpel password tjek!
            System.out.println("Service: Bruger " + username + " autentificeret.");
            return user;
        }
        System.out.println("Service: Autentificering fejlede for " + username);
        return null;
    }
}