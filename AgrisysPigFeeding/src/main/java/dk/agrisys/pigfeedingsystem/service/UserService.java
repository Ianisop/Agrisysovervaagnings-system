package dk.agrisys.pigfeedingsystem.service;

    import dk.agrisys.pigfeedingsystem.dao.UserDAO;
    import dk.agrisys.pigfeedingsystem.model.User;
    import dk.agrisys.pigfeedingsystem.model.UserRole;

    public class UserService {

        private final UserDAO userDao;

        public UserService() {
            this.userDao = new UserDAO();
        }

        public boolean createUser(String username, String password, UserRole userRole) {
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                return false; // Validation failed
            }

            UserRole role = UserRole.USER;

            User user = new User(username, password, role);
            return userDao.registerUserInDb(username, password, role, Integer.parseInt(user.getId())); // Save user via DAO
        }

        public UserDAO getDAO()
        {
            return userDao;
        }
    }