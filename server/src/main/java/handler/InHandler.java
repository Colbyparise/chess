package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.UserDAO;
import network.http.LoginRequest;
import service.Login;
import spark.Request;
import spark.Response;

public class InHandler {

    private final Login service;

    public InHandler(UserDAO userDAO, AuthDAO authDAO) {
        this.service = new Login(userDAO, authDAO);
    }

    public String login(Request req, Response res, Gson gson) throws DataAccessException {
        LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
        var result = service.login(loginRequest);
        return gson.toJson(result);
    }
}
