package service;
import chess.ChessGame;
import dataaccess.AuthDAO;
import model.AuthData;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.util.Objects;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;


    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public HashSet<GameData> listGames(String authToken) throws DataAccessException {
        validateAuth(authToken);
        return gameDAO.listGames(); // Returns all games
    }


    public int createGame(String authToken, String gameName) throws DataAccessException {
        validateAuth(authToken);

        if (gameName == null || gameName.isBlank()) {
            throw new DataAccessException("Game name cannot be empty");
        }

        int gameID;
        do {
            gameID = ThreadLocalRandom.current().nextInt(1, 1000);
        } while (gameDAO.gameExists(gameID));

        gameDAO.createGame(new GameData(gameID, null, null, gameName, new ChessGame()));
        return gameID;
    }

    public boolean joinGame(String authToken, int gameID, String color) throws DataAccessException {
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

        gameDAO.updateGame(new GameData(gameID, updatedWhiteUser, updatedBlackUser, gameData.gameName(), gameData.game()));
        return true;
    }

    public void clear() {
        gameDAO.clear();
    }

    private AuthData validateAuth(String authToken) throws DataAccessException {
        try {
            return authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private GameData getGameData(int gameID) throws DataAccessException {
        try {
            return gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private void validateColor(String color) throws DataAccessException {
        if (color == null || color.isBlank()) {
            throw new DataAccessException("Team color cannot be null or empty");
        }
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            throw new DataAccessException(color + " is not a valid team color");
        }
    }
}