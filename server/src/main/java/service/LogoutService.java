package service;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;

import network.http.Logout;

public class LogoutService {
    private AuthDAO authDAO;

    public LogoutService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public void logout(Logout request) throws DataAccessException {
        var authData = authDAO.authenticate(request.authToken());

        authDAO.deleteAuth(authData);
    }
}