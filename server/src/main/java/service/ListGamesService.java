package service;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;

import network.http.GetGames;
import network.http.ListOfGames;

public class ListGamesService {
    AuthDAO authDAO;
    GameDAO gameDAO;

    public ListGamesService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public ListOfGames listGames(GetGames request) throws DataAccessException {
        var auth = authDAO.authenticate(request.authToken());
        return new ListOfGames(gameDAO.listGames());
    }
}