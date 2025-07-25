package dataaccess;

import model.UserData;

import java.util.HashSet;

public class MemoryUserDAO implements UserDAO {
    private HashSet<UserData> db;

    public MemoryUserDAO() {
        db = HashSet.newHashSet(6);
    }


    @Override
    public void createUser(UserData userData) throws DataAccessException {
        try {
            getUser(userData.username());
        }
        catch (DataAccessException exception) {
            db.add(userData);
            return;
        }
        throw new DataAccessException("User already exists: " + userData.username());
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        for (UserData user : db) {
            if (user.username().equals(username)) {
                return user.password().equals(password);
            }
        }
        throw new DataAccessException("User does not exist: " + username);
    }


    @Override
    public UserData getUser(String username) throws DataAccessException {
        for (UserData userData : db) {
            if (userData.username().equals(username)) {
                return userData;
            }
        }
        throw new DataAccessException("User not found: " + username);
    }
    //remove userdata
    @Override
    public void clear() {
        db = HashSet.newHashSet(16);
    }
}
