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

    UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Object register(Request req, Response resp) throws BadRequestException {

        UserData userData = new Gson().fromJson(req.body(), UserData.class);

        if (userData.username() == null || userData.password() == null) {
            throw new BadRequestException("No username and/or password given");
        }

        try {
            AuthData authData = userService.createUser(userData);
            resp.status(200);
            return new Gson().toJson(authData);
        } catch (BadRequestException e) {
            resp.status(403);
            return "{ \"message\": \"Error: already taken\" }";
        }
    }

    public Object login(Request req, Response resp) throws UnauthorizedException, BadRequestException {
        UserData userData = new Gson().fromJson(req.body(), UserData.class);

        AuthData authData = userService.loginUser(userData);

        resp.status(200);
        return new Gson().toJson(authData);
    }

    public Object logout(Request req, Response resp) throws UnauthorizedException {
        String authToken = req.headers("authorization");

        userService.logoutUser(authToken);

        resp.status(200);
        return "{}";
    }

}