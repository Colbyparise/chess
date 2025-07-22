package dataaccess;


import model.GameData;
import chess.ChessGame;
import com.google.gson.Gson;
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
                    "PRIMARY KEY (gameID))";

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
            return null;
        }

        return games;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("INSERT INTO game " +
                    "(gameID, whiteUsername, blackUsername, gameName, chessGame) " +
                    "VALUES(?, ?, ?, ?, ?)")) {
                statement.setInt(1, game.gameID());
                statement.setString(2, game.whiteUsername());
                statement.setString(3, game.blackUsername());
                statement.setString(4, game.gameName());
                statement.setString(5, serializeGame(game.game()));
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException exception) {
            throw new DataAccessException("Error creating game ", exception);
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
                } else {
                    // If no game is found, throw DataAccessException
                    throw new DataAccessException("Game not found: " + gameID);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game: " + gameID);
        }
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
    public void updateGame(GameData game) throws DataAccessException {
        String updateSQL = "UPDATE game SET whiteUsername=?, blackUsername=?, gameName=?, chessGame=? WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(updateSQL)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, serializeGame(game.game()));
            stmt.setInt(5, game.gameID());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("No game found with gameID: " + game.gameID());
            }

        } catch (SQLException | DataAccessException exception) {
            throw new DataAccessException("Error updating game ", exception);
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