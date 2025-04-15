package dataaccess;

import dataaccess.interfaces.AuthDAO;
import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {

    private final Connection dbConnection;
    private final String authTable;

    public SQLAuthDAO(Connection dbConnection) {
        this.dbConnection = dbConnection;
        this.authTable = DatabaseManager.TABLES[DatabaseManager.TableName.Auth.ordinal()];
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        final String insertQuery = "INSERT INTO " + authTable + " (authToken, username) VALUES (?, ?);";
        try (PreparedStatement statement = dbConnection.prepareStatement(insertQuery)) {
            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create auth token: " + e.getMessage());
        }
    }

    @Override
    public AuthData authenticate(String token) throws DataAccessException {
        final String selectQuery = "SELECT authToken, username FROM " + authTable + " WHERE authToken = ?;";
        try (PreparedStatement statement = dbConnection.prepareStatement(selectQuery)) {
            statement.setString(1, token);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new AuthData(
                        result.getString("authToken"),
                        result.getString("username")
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Auth lookup failed: " + e.getMessage());
        }

        throw new UnauthorizedException("Error: Unauthorized access - invalid auth token");
    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException {
        final String deleteQuery = "DELETE FROM " + authTable + " WHERE authToken = ?;";
        try (PreparedStatement statement = dbConnection.prepareStatement(deleteQuery)) {
            statement.setString(1, auth.authToken());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth token: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        ClearUtil.clearTable(authTable, dbConnection);
    }
}
