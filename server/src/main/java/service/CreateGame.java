package service;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import network.http.GameRequest;
import network.http.GameResult;

public class CreateGame {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public CreateGame (AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public GameResult createGame(GameRequest request) throws DataAccessException {
        if (request == null || request.authToken() == null || request.gameName() == null) {
            throw new BadRequestException("Error: bad request");
        }

        var userSession = authDAO.authenticate(request.authToken());
        int newGameId = gameDAO.createGame(userSession, request.gameName());

        return new GameResult(newGameId);
    }
}
