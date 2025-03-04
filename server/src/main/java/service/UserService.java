package service;

import dataaccess.*;
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

    public AuthData createUser(UserData userData) throws BadRequestException {
        try {
            userDAO.createUser(userData);
        } catch (DataAccessException e) {
            throw new BadRequestException("User creation failed: " + e.getMessage());
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(userData.username(), authToken);
        authDAO.addAuth(authData);

        return authData;
    }

    public AuthData loginUser(UserData userData) throws UnauthorizedException, BadRequestException {
        boolean isAuthenticated;
        try {
            isAuthenticated = userDAO.authenticateUser(userData.username(), userData.password());
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

        if (!isAuthenticated) {
            throw new UnauthorizedException();
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(userData.username(), authToken);
        authDAO.addAuth(authData);

        return authData;
    }

    public void logoutUser(String authToken) throws UnauthorizedException {
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

        authDAO.deleteAuth(authToken);
    }

    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}
