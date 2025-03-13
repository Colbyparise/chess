package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;

import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public HashSet<GameData> listGames(String authToken) throws UnauthorizedException {
        validateAuth(authToken);
        return gameDAO.listGames();
    }

    public int createGame(String authToken, String gameName) throws UnauthorizedException {
        validateAuth(authToken);

        if (gameName == null || gameName.isBlank()) {
            throw new BadRequestException("Game name cannot be empty");
        }

        int gameID;
        do {
            gameID = ThreadLocalRandom.current().nextInt(1, 10000);
        } while (gameDAO.gameExists(gameID));
        try {
            gameDAO.createGame(new GameData(gameID, null, null, gameName, new ChessGame()));
        } catch (DataAccessException exception) {
            throw new BadRequestException(exception.getMessage());
        }
            return gameID;
    }

    public boolean joinGame(String authToken, int gameID, String color) throws UnauthorizedException, BadRequestException {
        AuthData authData = validateAuth(authToken);
        GameData gameData = getGameData(gameID);

        validateColor(color);

        String updatedWhiteUser = gameData.whiteUsername();
        String updatedBlackUser = gameData.blackUsername();

        if (Objects.equals(color, "WHITE")) {
            if (updatedWhiteUser != null) {
                return false;
            } else {
                updatedWhiteUser = authData.username();
            }
        } else {
            if (updatedBlackUser != null) {
                return false;
            } else {
                updatedBlackUser = authData.username();
            }
        }
        try {
            gameDAO.updateGame(new GameData(gameID, updatedWhiteUser, updatedBlackUser, gameData.gameName(), gameData.game()));
        } catch (DataAccessException exception) {
            throw new BadRequestException(exception.getMessage());
            }
            return true;
    }

    public void clear() {
        gameDAO.clear();
    }

    private AuthData validateAuth(String authToken) throws UnauthorizedException {
        try {
            return authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }
    }

    private GameData getGameData(int gameID) throws BadRequestException {
        try {
            return gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private void validateColor(String color) throws BadRequestException {
        if (color == null || color.isBlank()) {
            throw new BadRequestException("Team color cannot be null or empty");
        }
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            throw new BadRequestException(color + " is not a valid team color");
        }
    }
}
