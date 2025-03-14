package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;

import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class GameService {

    GameDAO gameDAO;
    AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public HashSet<GameData> listGames(String authToken) throws UnauthorizedException {
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }
        return gameDAO.listGames();
    }

    public int createGame(String authToken, String gameName) throws UnauthorizedException {
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

        int gameID;
        do { // Get random gameIDs until the gameID is not already in use
            gameID = ThreadLocalRandom.current().nextInt(1, 10000);
        } while (gameDAO.gameExists(gameID));

        gameDAO.createGame(new GameData(gameID, null, null, gameName, null));

        return gameID;
    }

    /***
     *
     * @param authToken authToken of user
     * @param gameID gameID of the game to join
     * @param color (nullable) team color to join as
     * @return boolean of success
     * @throws UnauthorizedException invalid authToken
     * @throws BadRequestException bad request
     */
    public boolean joinGame(String authToken, int gameID, String color) throws UnauthorizedException, BadRequestException {
        AuthData authData;
        GameData gameData;
        try {
            authData = authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

        try {
            gameData = gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }

        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();

        if (Objects.equals(color, "WHITE")) {
            if (whiteUser != null) return false; // Spot taken
            else whiteUser = authData.username();
        } else if (Objects.equals(color, "BLACK")) {
            if (blackUser != null) return false; // Spot taken
            else blackUser = authData.username();
        } else if (color != null) throw new BadRequestException("%s is not a valid team color".formatted(color));

        gameDAO.updateGame(new GameData(gameID, whiteUser, blackUser, gameData.gameName(), gameData.game()));
        return true;
    }

    public void clear() {
        gameDAO.clear();
    }
}