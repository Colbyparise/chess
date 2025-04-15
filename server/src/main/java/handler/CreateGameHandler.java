package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import network.http.GameRequest;
import service.CreateGame;
import spark.Request;
import spark.Response;

public class CreateGameHandler {
    private CreateGame service;

    public CreateGameHandler(AuthDAO authDAO, GameDAO gameDAO) {
        service = new CreateGame(authDAO, gameDAO);
    }

    public String createGame(Request req, Response res, Gson gson) throws DataAccessException {
        var body = gson.fromJson(req.body(), GameRequest.class);
        var createRequest = new GameRequest(req.headers("authorization"), body.gameName());

        var result = service.createGame(createRequest);
        return gson.toJson(result);
    }
}