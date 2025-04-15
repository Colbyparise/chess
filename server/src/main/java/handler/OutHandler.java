package handler;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import network.http.Logout;
import service.LogoutService;
import spark.Request;
import spark.Response;

public class OutHandler {

    private final LogoutService service;

    public OutHandler(AuthDAO authDAO) {
        this.service = new LogoutService(authDAO);
    }

    public String logout(Request req, Response res) throws DataAccessException {
        Logout logoutRequest = new Logout(req.headers("authorization"));
        service.logout(logoutRequest);
        return "{}";
    }
}
