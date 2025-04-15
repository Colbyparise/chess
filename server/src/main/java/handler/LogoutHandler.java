package handler;

import dataaccess.interfaces.AuthDAO;
import service.LogoutService;
import dataaccess.DataAccessException;

import network.http.Logout;
import spark.Request;
import spark.Response;

public class LogoutHandler {
    LogoutService service;

    public LogoutHandler(AuthDAO authDAO) {
        service = new LogoutService(authDAO);
    }

    public String logout(Request req, Response res) throws DataAccessException {
        var logoutRequest = new Logout(req.headers("authorization"));
        service.logout(logoutRequest);
        return "{}";
    }
}