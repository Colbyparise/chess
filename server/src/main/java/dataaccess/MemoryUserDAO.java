package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {

    private final Map<String, UserData> userTable = new HashMap<>();

    public MemoryUserDAO() {}


    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData user = userTable.get(username);
        if (user == null) {
            throw new DataAccessException("No user found for username: " + username);
        }
        return user;
    }

    @Override
    public void createUser(UserData newUser) throws DataAccessException {
        String username = newUser.username();
        if (userTable.containsKey(username)) {
            throw new DataAccessException("Username already in use: " + username);
        }
        userTable.put(username, newUser);
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        UserData foundUser = userTable.get(username);

        if (foundUser == null) {
            throw new DataAccessException("Authentication failed — user not found: " + username);
        }

        return foundUser.password().equals(password);
    }

    @Override
    public void clear() {
        userTable.clear();
    }
}
