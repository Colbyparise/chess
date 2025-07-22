package server;

import model.AuthData;
import model.UserData;
import dataaccess.ErrorResponse;
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

    public Object registrationHandler(Request req, Response resp) {
        UserData userData = gson.fromJson(req.body(), UserData.class);

        if (userData == null || invalidUserData(userData)) {
            resp.status(400);
            return gson.toJson(new ErrorResponse("Error: Missing username or password"));
        }

        try {
            AuthData authData = userService.createUser(userData);
            resp.status(200);
            return gson.toJson(authData);
        } catch (DataAccessException exception) {
            if (exception.getMessage().toLowerCase().contains("already taken")
                    || exception.getMessage().toLowerCase().contains("error creating user")
                    || exception.getMessage().toLowerCase().contains("existinguser")) {
                resp.status(403);
                return gson.toJson(new ErrorResponse("Error: already taken"));
            } else {
                resp.status(500);
                return gson.toJson(new ErrorResponse("Error: " + exception.getMessage()));
            }
        }
    }

    public Object loginHandler(Request req, Response resp) {
        try {
            UserData userData = gson.fromJson(req.body(), UserData.class);

            if (userData == null || invalidUserData(userData)) {
                resp.status(400);
                return gson.toJson(new ErrorResponse("Error: Missing username or password"));
            }

            AuthData authData = userService.loginUser(userData);
            resp.status(200);
            return gson.toJson(authData);

        } catch (DataAccessException exception) {
            if (exception.getMessage().toLowerCase().contains("invalid")
                    || exception.getMessage().toLowerCase().contains("user not found")) {
                resp.status(401);
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            } else {
                resp.status(500);
                return gson.toJson(new ErrorResponse("Error: " + exception.getMessage()));
            }
        }
    }

    public Object logoutHandler(Request req, Response resp) {
        try {
            String authToken = req.headers("authorization");

            if (authToken == null || authToken.isBlank()) {
                resp.status(401);
                return gson.toJson(new ErrorResponse("Error: Invalid or missing authToken."));
            }
            userService.logoutUser(authToken);
            resp.status(200);
            return "{}";
        } catch (DataAccessException exception) {
            resp.status(500);
            return gson.toJson(new ErrorResponse("Error: Invalid or mission authToken."));
        }
    }

    private boolean invalidUserData(UserData userData) {
        return userData.username() == null || userData.username().isBlank()
                || userData.password() == null || userData.password().isBlank();
    }
}
