package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.UserDAO;
import model.UserData;
import network.http.Register;
import service.RegisterService;
import spark.Request;
import spark.Response;

public class RegHandler {

    private final RegisterService service;

    public RegHandler(UserDAO userDAO, AuthDAO authDAO) {
        this.service = new RegisterService(userDAO, authDAO);
    }

    public String register(Request req, Response res, Gson gson) throws DataAccessException {
        UserData userData = gson.fromJson(req.body(), UserData.class);
        Register registerRequest = new Register(userData);

        var result = service.register(registerRequest);
        return gson.toJson(result);
    }
}
