package dataAccess;
import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {

    private final Map<String, UserData> db;

    public MemoryUserDAO() {
        db = new HashMap<>();
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData user = db.get(username);
        if (user == null) {
            throw new DataAccessException("User not found: " + username);
        }
        return user;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (db.containsKey(user.username())) {
            throw new DataAccessException("User already exists: " + user.username());
        }
        db.put(user.username(), user);
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        UserData user = db.get(username);
        if (user == null) {
            throw new DataAccessException("User does not exist: " + username);
        }
        return user.password().equals(password);
    }

    @Override
    public void clear() {
        db.clear();
    }
}
