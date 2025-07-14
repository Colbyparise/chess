package server;

import model.AuthData;
import model.UserData;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.UserService;
import spark.Response;
import spark.Request;

//This java class contains the methods for user handlers
//including registration handler, loginhandler and logouthandler

public class UserHandler {
    private final UserService userService;
    private final Gson gson;

    public UserHandler(UserService userService) {
        this.userService = userService;
        this.gson = new Gson();
    }

    public Object registrationHandler(Request req, Response resp) throws DataAccessException {
        UserData userData = gson.fromJson(req.body(), UserData.class);

        if (userData == null || invalidUserData(userData)) {
            throw new DataAccessException("Missing username or password");
        }

        try {
            AuthData authData = userService.createUser(userData);
            resp.status(200);
            return gson.toJson(authData);
        } catch (DataAccessException exception) {
            resp.status(403);
            return errorResponse();
        }
    }

    public Object loginHandler(Request req, Response resp) throws DataAccessException {

        UserData userData = gson.fromJson(req.body(), UserData.class);

            if(userData ==null || invalidUserData(userData)) {
                throw new DataAccessException("Missing username or password");
            }

            AuthData authData = userService.loginUser(userData);
            resp.status(200);
            return gson.toJson(authData);
    }

    public Object logoutHandler(Request req, Response resp) throws DataAccessException {
        String authToken = req.headers("authorization");

        if (authToken == null || authToken.isBlank()) {
            throw new DataAccessException();
        }
        userService.logoutUser(authToken);
        resp.status(200);
        return "{}";
    }
    private boolean invalidUserData(UserData userData) {
        return userData.username() == null || userData.username().isBlank()
                || userData.password() == null || userData.password().isBlank();
    }

    private String errorResponse() {
        return "{ \"message\": \"Error: " + "Username already taken" + "\" }";
    }
}
