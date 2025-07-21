package service;


import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.UserDAO;
import model.UserData;
import model.AuthData;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData createUser(UserData userData) throws DataAccessException {
        try {
            userDAO.createUser(userData);
        } catch (DataAccessException exception) {
            throw new DataAccessException("User creation failed: " + exception.getMessage());
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(userData.username(), authToken);
        authDAO.createAuth(authData);

        return authData;
    }
    public AuthData loginUser(UserData userData) throws DataAccessException, UnauthorizedException {
        boolean isAuthenticated;
        try {
            isAuthenticated = userDAO.authenticateUser(userData.username(), userData.password());
        } catch (DataAccessException exception) {
            throw new DataAccessException("Authentication failed: " + exception.getMessage());
        }

        if (!isAuthenticated) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(userData.username(), authToken);
        authDAO.createAuth(authData);

        return authData;
    }

    public void logoutUser(String authToken) throws DataAccessException, UnauthorizedException{
        AuthData auth;
        try {
            auth = authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw e;
        }

        if (auth == null) {
            throw new UnauthorizedException("Invalid or missing auth token.");
        }

        authDAO.deleteAuth(authToken);
    }


    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}

