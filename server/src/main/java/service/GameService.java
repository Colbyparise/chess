package service;
import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.UnauthorizedException;
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
        return new HashSet<>(gameDAO.listGames()); // Returns all games
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

    public void observeGame(String authToken, int gameID) throws DataAccessException {
        // Validate auth token
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Auth Token does not exist");
        }
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }
    }

    public boolean joinGame(String authToken, int gameID, String color) throws DataAccessException {
        AuthData authData = validateAuth(authToken);
        GameData gameData = getGameData(gameID);
        validateColor(color);

        String username = authData.username();
        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();

        if (color.equalsIgnoreCase("WHITE")) {
            if (white != null && !white.equals(username)) {
                return false;
            }
            white = username;
        } else if (color.equalsIgnoreCase("BLACK")) {
            if (black != null && !black.equals(username)) {
                return false;
            }
            black = username;
        }

        gameDAO.updateGame(new GameData(
                gameID,
                white,
                black,
                gameData.gameName(),
                gameData.game()
        ));
        return true;
    }


    public void clear() {
        gameDAO.clear();
    }

    private AuthData validateAuth(String authToken) throws UnauthorizedException, DataAccessException {
        AuthData auth;
        try {
            auth = authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            // Real DB error → must be treated as 500
            throw e;
        }

        if (auth == null) {
            // Token wasn't found, but DB was reachable → 401
            throw new UnauthorizedException("Invalid auth token");
        }

        return auth;
    }

    private GameData getGameData(int gameID) throws DataAccessException {
        try {
            return gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public ChessGame getGame(String authToken, int gameID) throws DataAccessException {
        validateAuth(authToken); // Ensure the request is authorized
        GameData gameData = getGameData(gameID); // Retrieve game info
        if (gameData == null) {
            throw new DataAccessException("Game not found");
        }
        return gameData.game(); // Return the ChessGame object
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