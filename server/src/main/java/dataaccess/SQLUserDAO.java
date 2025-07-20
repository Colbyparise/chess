package dataaccess;

import model.UserData;

public class SQLUserDAO implements UserDAO {

    public SQLUSerDAO() {
        initializeDatabase();
    }
    private void initializeDatabase() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException exception) {
            throw new RuntimeException("Failed to create database: " + exception.getMessage(), exception);
        }

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return new UserData(username, password, email);
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {

    }
    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        return true;
    }
    @Override
    public void clear() {

    }

}
