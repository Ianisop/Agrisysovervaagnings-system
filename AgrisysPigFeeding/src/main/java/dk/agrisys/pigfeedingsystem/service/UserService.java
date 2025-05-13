package dk.agrisys.pigfeedingsystem.service;

    import dk.agrisys.pigfeedingsystem.Generator;
    import dk.agrisys.pigfeedingsystem.dao.InviteCodeDAO;
    import dk.agrisys.pigfeedingsystem.dao.UserDAO;
    import dk.agrisys.pigfeedingsystem.model.User;
    import dk.agrisys.pigfeedingsystem.model.UserRole;

    public class UserService {

        private final UserDAO userDao;

        public UserService() {
            this.userDao = new UserDAO();
        }

        public boolean createUser(String username, String password, String inviteCode) {
            if (username == null || username.isEmpty() || password == null || password.isEmpty() || userDao.getUser(username) != null) {
                System.out.println("USER ALREADY EXISTS");
                return false; // Validation failed
            }

            InviteCodeDAO ico = new InviteCodeDAO();
            UserRole role = ico.getInviteCodeRoleType(inviteCode) ? UserRole.SUPERUSER : UserRole.USER;


            //User user = new User(username, password, role, Generator.generate(8));
            return userDao.registerUserInDb(username, password, role, Integer.parseInt(Generator.generate(8)), inviteCode); // Save user via DAO
        }

        public UserDAO getDAO()
        {
            return userDao;
        }
    }