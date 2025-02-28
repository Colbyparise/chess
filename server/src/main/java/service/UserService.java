package service;

import dataAccess.*;

import model.AuthData;

import model.UserData;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService() {
        this.userDAO = new MemoryUserDAO();  // Use default in-memory DAO
        this.authDAO = new MemoryAuthDAO();
    }

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = new MemoryUserDAO();
        this.authDAO = new MemoryAuthDAO();
    }


    public AuthData createUser(UserData userData) throws BadRequestException {

        try {
            userDAO.createUser(userData);
        } catch (DataAccessException exception) {
            throw new BadRequestException(exception.getMessage());
    }

    String authToken = UUID.randomUUID().toString();
    AuthData authData = new AuthData(userData.username(), authToken);
    authDAO.addAuth(authData);

    return authData;
}

    public AuthData loginUser(UserData userData) throws UnauthorizedException {
        boolean userAuthenticated;
        try {
            userAuthenticated = userDAO.authenticateUser(userData.username(), userData.password());
        } catch (DataAccessException exception) {
            throw new UnauthorizedException();
        }
        if (userAuthenticated) {
            String authToken = UUID.randomUUID().toString();
            AuthData authData = new AuthData(userData.username(), authToken);
            authDAO.addAuth(authData);
            return authData;
        }
        else {
            throw new UnauthorizedException();
        }
    }
    public void logoutUser(String authToken) throws UnauthorizedException {
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException exception) {
            throw new UnauthorizedException();
        }
        authDAO.deleteAuth(authToken);
        }

        public AuthData getAuth(String authToken) throws UnauthorizedException {
        try {
            return authDAO.getAuth(authToken);
        } catch (DataAccessException exception) {
            throw new UnauthorizedException();
        }
    }
    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}