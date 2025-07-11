package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void createAuth(AuthData authData);
    void deleteAuth(String authToken);
    AuthData getAuth(String authToken) throws DataAccessException;
    void clear();
}
