package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import network.http.GameRequest;
import service.CreateGameService;
import spark.Request;
import spark.Response;

public class CreateGameHandler {
    private CreateGameService service;

    public CreateGameHandler(AuthDAO authDAO, GameDAO gameDAO) {
        service = new CreateGameService(authDAO, gameDAO);
    }

    public String createGame(Request req, Response res, Gson gson) throws DataAccessException {
        var body = gson.fromJson(req.body(), GameRequest.class);
        var createRequest = new GameRequest(req.headers("authorization"), body.gameName());

        var result = service.createGame(createRequest);
        return gson.toJson(result);
    }
}