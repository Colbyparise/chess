package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }


    public AuthData createUser(UserData userData) throws DataAccessException {
        if (userData == null || userData.username() == null || userData.password() == null) {
            throw new DataAccessException("Missing user data");
        }

        userDAO.createUser(userData);
        AuthData auth = generateToken(userData.username());
        authDAO.createAuth(auth);

        return auth;
    }


    public AuthData loginUser(UserData userData) throws DataAccessException, UnauthorizedException {
        if (userData == null || userData.username() == null || userData.password() == null) {
            throw new UnauthorizedException("Missing credentials");
        }

        boolean authenticated = userDAO.authenticateUser(userData.username(), userData.password());
        if (!authenticated) {
            throw new UnauthorizedException("Invalid username or password");
        }

        AuthData auth = generateToken(userData.username());
        authDAO.createAuth(auth);

        return auth;
    }


    public void logoutUser(String authToken) throws DataAccessException, UnauthorizedException {
        AuthData existingAuth = authDAO.getAuth(authToken);
        if (existingAuth == null) {
            throw new UnauthorizedException("Auth token is invalid or expired");
        }

        authDAO.deleteAuth(authToken);
    }


    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }

    private AuthData generateToken(String username) {
        String token = UUID.randomUUID().toString();
        return new AuthData(username, token);
    }
}
