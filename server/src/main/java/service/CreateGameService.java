package service;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;

import network.http.GameRequest;
import network.http.GameResult;

public class CreateGameService {
    AuthDAO authDAO;
    GameDAO gameDAO;

    public CreateGameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public GameResult createGame(GameRequest request) throws DataAccessException {
        if (request == null || request.authToken() == null || request.gameName() == null) {
            throw new BadRequestException("Error: bad request");
        }

        var auth = authDAO.authenticate(request.authToken());
        int id = gameDAO.createGame(auth, request.gameName());
        return new GameResult(id);
    }
}