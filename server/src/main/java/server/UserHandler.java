package server;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.UserData;
import service.UserService;
import spark.Request;
import spark.Response;

public class UserHandler {

    private final UserService userService;
    private final Gson gson;

    public UserHandler(UserService userService) {
        this.userService = userService;
        this.gson = new Gson();
    }

    public Object handleUserRegistration(Request req, Response resp) throws BadRequestException {
        UserData userData = gson.fromJson(req.body(), UserData.class);

        if (userData == null || isInvalidUserData(userData)) {
            throw new BadRequestException("Missing username or password");
        }

        try {
            AuthData authData = userService.createUser(userData);
            resp.status(200);
            return gson.toJson(authData);
        } catch (BadRequestException e) {
            resp.status(403);
            return generateErrorResponse();
        }
    }

    public Object handleUserLogin(Request req, Response resp) throws UnauthorizedException, BadRequestException {
        UserData userData = gson.fromJson(req.body(), UserData.class);

        if (userData == null || isInvalidUserData(userData)) {
            throw new BadRequestException("Missing username or password");
        }

        AuthData authData = userService.loginUser(userData);
        resp.status(200);
        return gson.toJson(authData);
    }

    public Object handleUserLogout(Request req, Response resp) throws UnauthorizedException {
        String authToken = req.headers("authorization");

        if (authToken == null || authToken.isBlank()) {
            throw new UnauthorizedException();
        }

        userService.logoutUser(authToken);
        resp.status(200);
        return "{}";
    }

    private boolean isInvalidUserData(UserData userData) {
        return userData.username() == null || userData.username().isBlank()
                || userData.password() == null || userData.password().isBlank();
    }

    private String generateErrorResponse() {
        return "{ \"message\": \"Error: " + "Username already taken" + "\" }";
    }
}