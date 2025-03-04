package dataaccess;
import model.AuthData;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryAuthDAO implements AuthDAO {

    private final ConcurrentHashMap<String, AuthData> db;

    public MemoryAuthDAO() {
        this.db = new ConcurrentHashMap<>();
    }

    @Override
    public void addAuth(AuthData authData) {
        db.putIfAbsent(authData.authToken(), authData);
    }

    @Override
    public void deleteAuth(String authToken) {
        db.remove(authToken);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData authData = db.get(authToken);
        if (authData == null) {
            throw new DataAccessException("Auth Token does not exist: " + authToken);
        }
        return authData;
    }

    @Override
    public void clear() {
        db.clear();
    }
}
