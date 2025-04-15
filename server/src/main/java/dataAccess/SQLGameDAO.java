package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.interfaces.GameDAO;
import model.AuthData;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class SQLGameDAO implements GameDAO {

    private final Connection dbConnection;
    private final String gamesTable;
    private final Gson jsonConverter;

    public SQLGameDAO(Connection dbConnection) {
        this.dbConnection = dbConnection;
        this.gamesTable = DatabaseManager.TABLES[DatabaseManager.TableName.Games.ordinal()];
        this.jsonConverter = new Gson();
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        Collection<GameData> gameList = new ArrayList<>();
        final String query = "SELECT id, whiteUsername, blackUsername, gameName, chessGame FROM " + gamesTable + ";";

        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int gameId = resultSet.getInt("id");
                String whitePlayer = resultSet.getString("whiteUsername");
                String blackPlayer = resultSet.getString("blackUsername");
                String name = resultSet.getString("gameName");
                String gameJson = resultSet.getString("chessGame");

                ChessGame game = jsonConverter.fromJson(gameJson, ChessGame.class);
                gameList.add(new GameData(gameId, whitePlayer, blackPlayer, name, game));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to list games: " + e.getMessage());
        }

        return gameList;
    }

    @Override
    public int createGame(AuthData userAuth, String name) throws DataAccessException {
        final String insert = "INSERT INTO " + gamesTable + " (whiteUsername, blackUsername, gameName, chessGame) VALUES (?, ?, ?, ?);";
        int generatedId = 0;

        try (PreparedStatement statement = dbConnection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, null);
            statement.setString(2, null);
            statement.setString(3, name);
            statement.setString(4, jsonConverter.toJson(new ChessGame()));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 1) {
                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    generatedId = keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create game: " + e.getMessage());
        }

        return generatedId;
    }

    @Override
    public GameData getGame(AuthData requester, int gameId) throws DataAccessException {
        GameData result = null;
        final String query = "SELECT whiteUsername, blackUsername, gameName, chessGame FROM " + gamesTable + " WHERE id = ?;";

        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            statement.setInt(1, gameId);
            ResultSet res = statement.executeQuery();

            if (res.next()) {
                String white = res.getString("whiteUsername");
                String black = res.getString("blackUsername");
                String name = res.getString("gameName");
                String gameState = res.getString("chessGame");

                ChessGame chessGame = jsonConverter.fromJson(gameState, ChessGame.class);
                result = new GameData(gameId, white, black, name, chessGame);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to retrieve game: " + e.getMessage());
        }

        if (result == null) {
            throw new DataAccessException("Error: Game with ID " + gameId + " not found.");
        }

        return result;
    }

    @Override
    public void updateGame(GameData updatedData) throws DataAccessException {
        final String update = "UPDATE " + gamesTable + " SET whiteUsername = ?, blackUsername = ?, gameName = ?, chessGame = ? WHERE id = ?;";

        try (PreparedStatement statement = dbConnection.prepareStatement(update)) {
            statement.setString(1, updatedData.whiteUsername());
            statement.setString(2, updatedData.blackUsername());
            statement.setString(3, updatedData.gameName());
            statement.setString(4, jsonConverter.toJson(updatedData.game()));
            statement.setInt(5, updatedData.gameID());

            int updatedRows = statement.executeUpdate();
            if (updatedRows != 1) {
                throw new DataAccessException("Game with ID " + updatedData.gameID() + " could not be updated.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    public void deleteGame(int gameId) throws DataAccessException {
        final String deleteQuery = "DELETE FROM " + gamesTable + " WHERE id = ?;";

        try (PreparedStatement statement = dbConnection.prepareStatement(deleteQuery)) {
            statement.setInt(1, gameId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete game: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        ClearUtil.clearTable(gamesTable, dbConnection);
    }
}