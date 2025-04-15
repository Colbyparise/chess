package handler;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.UserDAO;
import service.Login;
import com.google.gson.Gson;
import dataaccess.DataAccessException;

import network.http.LoginRequest;
import spark.Request;
import spark.Response;

public class LoginHandler {
    Login service;

    public LoginHandler(UserDAO userDAO, AuthDAO authDAO) {

        service = new Login(userDAO, authDAO);
    }

    public String login(Request req, Response res, Gson gson) throws DataAccessException {
        var loginRequest = gson.fromJson(req.body(), LoginRequest.class);
        var result = service.login(loginRequest);
        return gson.toJson(result);
    }
}