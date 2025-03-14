package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.SQLException;
import java.util.HashSet;

public class SQLGameDAO implements GameDAO {

    public SQLGameDAO() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException ex) {
            throw new RuntimeException("Error creating database: " + ex.getMessage(), ex);
        }

        try (var conn = DatabaseManager.getConnection()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS game (" +
                    "gameID INT NOT NULL, " +
                    "whiteUsername VARCHAR(255), " +
                    "blackUsername VARCHAR(255), " +
                    "gameName VARCHAR(255), " +
                    "chessGame TEXT, " +
                    "PRIMARY KEY (gameID))";

            try (var stmt = conn.prepareStatement(createTableSQL)) {
                stmt.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error setting up game table: " + e.getMessage(), e);
        }
    }

    @Override
    public HashSet<GameData> listGames() {
        HashSet<GameData> games = new HashSet<>(16);

        try (var conn = DatabaseManager.getConnection()) {
            String query = "SELECT gameID, whiteUsername, blackUsername, gameName, chessGame FROM game";
            try (var stmt = conn.prepareStatement(query);
                 var resultSet = stmt.executeQuery()) {

                while (resultSet.next()) {
                    var gameID = resultSet.getInt("gameID");
                    var whiteUsername = resultSet.getString("whiteUsername");
                    var blackUsername = resultSet.getString("blackUsername");
                    var gameName = resultSet.getString("gameName");
                    var chessGame = deserializeGame(resultSet.getString("chessGame"));
                    games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame));
                }
            }
        } catch (SQLException | DataAccessException e) {
            return null;  // Returning null in case of any issues.
        }

        return games;
    }

    @Override
    public void createGame(GameData game) {
        String insertSQL = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, chessGame) VALUES (?, ?, ?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(insertSQL)) {

            stmt.setInt(1, game.gameID());
            stmt.setString(2, game.whiteUsername());
            stmt.setString(3, game.blackUsername());
            stmt.setString(4, game.gameName());
            stmt.setString(5, serializeGame(game.game()));
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            // Handle exception if needed.
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String query = "SELECT whiteUsername, blackUsername, gameName, chessGame FROM game WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, gameID);
            try (var resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    var whiteUsername = resultSet.getString("whiteUsername");
                    var blackUsername = resultSet.getString("blackUsername");
                    var gameName = resultSet.getString("gameName");
                    var chessGame = deserializeGame(resultSet.getString("chessGame"));
                    return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Game not found, id: " + gameID);
        }

        return null;  // In case no game is found.
    }

    @Override
    public boolean gameExists(int gameID) {
        String query = "SELECT gameID FROM game WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, gameID);
            try (var resultSet = stmt.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException | DataAccessException e) {
            return false;
        }
    }

    @Override
    public void updateGame(GameData game) {
        String updateSQL = "UPDATE game SET whiteUsername=?, blackUsername=?, gameName=?, chessGame=? WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(updateSQL)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, serializeGame(game.game()));
            stmt.setInt(5, game.gameID());
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            // Handle exception if needed.
        }
    }

    @Override
    public void clear() {
        try (var conn = DatabaseManager.getConnection()) {
            try (var stmt = conn.prepareStatement("TRUNCATE game")) {
                stmt.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error clearing the game table: " + e.getMessage(), e);
        }
    }

    private String serializeGame(ChessGame game) {
        return new Gson().toJson(game);
    }

    private ChessGame deserializeGame(String serializedGame) {
        return new Gson().fromJson(serializedGame, ChessGame.class);
    }
}
