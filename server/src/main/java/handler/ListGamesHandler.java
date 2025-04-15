package handler;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import service.ListGamesService;
import com.google.gson.Gson;
import dataaccess.DataAccessException;

import network.http.GetGames;
import spark.Request;
import spark.Response;

public class ListGamesHandler {
    ListGamesService service;

    public ListGamesHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.service = new ListGamesService(authDAO, gameDAO);
    }

    public String listGames(Request req, Response res, Gson gson) throws DataAccessException {
        var listRequest = new GetGames(req.headers("authorization"));
        var result = service.listGames(listRequest);
        return gson.toJson(result);
    }

}