package dataaccess;

import model.UserData;

public interface UserDAO {
    void createUser(UserData userData) throws DataAccessException; //create a new user
    UserData getUser(String username) throws DataAccessException; //retrieve a user with the given username
    boolean authenticateUser(String username, String password) throws DataAccessException;
    void clear();
}
