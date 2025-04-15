package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import network.http.GetGames;
import service.ListGames;
import spark.Request;
import spark.Response;

public class ListGamesHandler {

    private final ListGames service;

    public ListGamesHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.service = new ListGames(authDAO, gameDAO);
    }

    public String listGames(Request req, Response res, Gson gson) throws DataAccessException {
        GetGames listRequest = new GetGames(req.headers("authorization"));
        var result = service.listGames(listRequest);
        return gson.toJson(result);
    }
}
