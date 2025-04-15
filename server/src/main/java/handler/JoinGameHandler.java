package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import network.http.JoinGame;
import service.JoinGameService;
import spark.Request;
import spark.Response;

public class JoinGameHandler {
    JoinGameService service;

    public JoinGameHandler(AuthDAO authDAO, GameDAO gameDAO) {

        service = new JoinGameService(authDAO, gameDAO);
    }

    public String joinGame(Request req, Response res, Gson gson) throws DataAccessException {
        var body = gson.fromJson(req.body(), JoinGame.class);
        var joinRequest = new JoinGame(req.headers("authorization"), body.playerColor(), body.gameID());
        service.joinGame(joinRequest);

        return "{}";
    }
}