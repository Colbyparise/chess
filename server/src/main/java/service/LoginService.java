package service;

import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.UserDAO;

import network.http.LoginRequest;
import network.http.LoginResult;
import org.mindrot.jbcrypt.BCrypt;

public class LoginService {
    UserDAO loginDAO;
    AuthDAO authDAO;

    public LoginService(UserDAO loginDAO, AuthDAO authDAO) {
        this.loginDAO = loginDAO;
        this.authDAO = authDAO;
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        var user = loginDAO.getUser(request.username());
        if (user == null || !BCrypt.checkpw(request.password(), user.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        var auth = AuthDAO.generateAuth(request.username());

        authDAO.createAuth(auth);

        return new LoginResult(auth.username(), auth.authToken());
    }
}