package dataaccess;


import model.GameData;
import chess.ChessGame;

import java.sql.SQLException;
import java.util.HashSet;

public class SQLGameDAO implements GameDAO {
    public SQLGameDAO() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException exception) {
            throw new RuntimeException("Error creating database: " + exception.getMessage(), exception);
        }

        try (var connection = DatabaseManager.getConnection()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS game (" +
                    "gameID INT NOT NULL, " +
                    "whiteUsername VARCHAR(255), " +
                    "blackUsername VARCHAR(255), " +
                    "gameName VARCHAR(255), " +
                    "chessGame TEXT, " +
                    "PRIMARY KEY (gameID)";

            try (var statement = connection.prepareStatement(createTableSQL)) {
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException exception) {
            throw new RuntimeException("Error setting up game table: " + exception.getMessage(), exception);
        }
    }

    @Override
    public HashSet<GameData> listGames() {
        HashSet<GameData> games = new HashSet<>(16);
    }

    @Override
    public void createGame(GameData gameData) {

    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame)
    }

    @Override
    public boolean gameExists(int gameID) {
        return true;
    }

    @Override
    public void updateGame(GameData gameData) {

    }

    @Override
    public void clear() {

    }

}

