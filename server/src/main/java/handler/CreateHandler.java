package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import network.http.GameRequest;
import service.CreateGame;
import spark.Request;
import spark.Response;

public class CreateHandler {

    private final CreateGame service;

    public CreateHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.service = new CreateGame(authDAO, gameDAO);
    }

    public String createGame(Request req, Response res, Gson gson) throws DataAccessException {
        GameRequest body = gson.fromJson(req.body(), GameRequest.class);
        GameRequest createRequest = new GameRequest(req.headers("authorization"), body.gameName());
        var result = service.createGame(createRequest);
        return gson.toJson(result);
    }
}
