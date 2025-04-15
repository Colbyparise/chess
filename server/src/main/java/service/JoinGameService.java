package service;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.DatabaseException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;

import model.GameData;
import network.http.JoinGame;

import java.util.Set;
import java.util.List;
import java.util.HashSet;

public class JoinGameService {

    private static final Set<String> VALID_COLORS = new HashSet<>(List.of("WHITE", "BLACK"));

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public JoinGameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void joinGame(JoinGame request) throws DataAccessException {
        var requestedColor = request.playerColor();
        var gameId = request.gameID();
        var token = request.authToken();

        if (token == null || requestedColor == null || gameId == 0) {
            throw new BadRequestException("Error: bad request");
        }

        requestedColor = requestedColor.toUpperCase();
        if (!VALID_COLORS.contains(requestedColor)) {
            throw new BadRequestException("Error: bad request");
        }

        var session = authDAO.authenticate(token);
        var game = gameDAO.getGame(session, gameId);

        String white = game.whiteUsername();
        String black = game.blackUsername();
        GameData updatedGame;

        if ("WHITE".equals(requestedColor)) {
            if (white != null) {
                throw new DatabaseException("Error: already taken");
            }
            updatedGame = new GameData(game.gameID(), session.username(), black, game.gameName(), game.game());
        } else {
            if (black != null) {
                throw new DatabaseException("Error: already taken");
            }
            updatedGame = new GameData(game.gameID(), white, session.username(), game.gameName(), game.game());
        }

        gameDAO.updateGame(updatedGame);
    }
}
