package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void createAuth(AuthData authData) throws DataAccessException; //Create a new authorization
    AuthData getAuth(String authToken) throws DataAccessException; //Retrieve an authorization given an authToken
    void deleteAuth(String authToken) throws DataAccessException; //Delete an authorization so that it is no longer valid
    void clear(); //clears data from database
}
