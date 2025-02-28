package dataAccess;

import model.UserData;

import java.util.HashSet;

public class MemoryUserDAO implements UserDAO {
    private HashSet<UserData> db;

    public MemoryUserDAO() {
        db = HashSet.newHashSet(16);
    }

    @Override
    public void getUser(String username) throws DataAccessException {
        for (UserData user : db) {
            if (user.username().equals(username)) {
                return;
            }
        }
        throw new DataAccessException("Username not found: " +username);
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try {
            getUser(user.username());
        }
        catch (DataAccessException exception) {
            db.add(user);
            return;
        }
        throw new DataAccessException("Username already exists: " + user.username());
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        boolean userExists = false;
        for (UserData user : db) {
            if (user.username().equals(username)) {
                userExists = true;
            }
            if (user.username().equals(username) &&
                    user.password().equals(password)) {
                    return true;
            }
        }
        if (userExists) {
            return false;
        }
        else {
            throw new DataAccessException("User does not exist: " + username);
        }
    }

    @Override
    public void clear() {
        db = HashSet.newHashSet(16);
    }
}
