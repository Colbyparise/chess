package service;

import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.UserDAO;

import network.http.LoginRequest;
import network.http.LoginResult;
import org.mindrot.jbcrypt.BCrypt;

public class Login {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public Login(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        var storedUser = userDAO.getUser(request.username());

        if (storedUser == null || !BCrypt.checkpw(request.password(), storedUser.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        var session = AuthDAO.generateAuth(storedUser.username());
        authDAO.createAuth(session);

        return new LoginResult(storedUser.username(), session.authToken());
    }
}
