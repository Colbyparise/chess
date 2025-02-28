package dataAccess;

import model.UserData;

public interface UserDAO {

    void getUser(String username) throws DataAccessException;
    void createUser(UserData user) throws DataAccessException;
    boolean authenticateUser(String username, String password) throws DataAccessException;
    void clear();
 }
