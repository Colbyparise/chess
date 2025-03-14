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
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object register(Request request, Response response) throws BadRequestException {
        UserData userData = gson.fromJson(request.body(), UserData.class);

        if (userData.username() == null || userData.password() == null) {
            throw new BadRequestException("No username and/or password provided.");
        }

        try {
            AuthData authData = userService.createUser(userData);
            response.status(200);
            return gson.toJson(authData);
        } catch (BadRequestException e) {
            response.status(403);
            return gson.toJson(new ErrorMessage("Error: already taken"));
        }
    }

    public Object login(Request request, Response response) throws UnauthorizedException, BadRequestException {
        UserData userData = gson.fromJson(request.body(), UserData.class);
        AuthData authData = userService.loginUser(userData);

        response.status(200);
        return gson.toJson(authData);
    }

    public Object logout(Request request, Response response) throws UnauthorizedException {
        String authToken = request.headers("authorization");
        userService.logoutUser(authToken);

        response.status(200);
        return gson.toJson(new EmptyResponse());
    }

    private static class ErrorMessage {

        public ErrorMessage(String message) {
        }
    }

    private static class EmptyResponse {}
}