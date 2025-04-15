package service;

import chess.ChessGame;
import chess.ChessBoard;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import java.util.HashSet;
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

    public GameData getGameData(String authToken, int gameID) throws UnauthorizedException, BadRequestException {
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

        try {
            return gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public void updateGame(String authToken, GameData gameData) throws UnauthorizedException, BadRequestException {
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

        try {
            gameDAO.updateGame(gameData);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public int createGame(String authToken, String gameName) throws UnauthorizedException, BadRequestException {
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

        int gameID;
        do { // Get random gameIDs until the gameID is not already in use
            gameID = ThreadLocalRandom.current().nextInt(1, 10000);
        } while (gameDAO.gameExists(gameID));

        try {
            ChessGame game = new ChessGame();
            ChessBoard board = new ChessBoard();
            board.resetBoard();
            game.setBoard(board);
            gameDAO.createGame(new GameData(gameID, null, null, gameName, game));
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }

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
        AuthData authData = validateAuth(authToken);
        GameData gameData = getGameData(gameID);


        validateColor(color);

        String updatedWhiteUser = gameData.whiteUsername();
        String updatedBlackUser = gameData.blackUsername();

        if (color.equals("WHITE")) {
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
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
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