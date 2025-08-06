package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {

    private static final String AUTH_TABLE = "auth";

    public SQLAuthDAO() {
        try {
            DatabaseManager.createDatabase();
            initializeTable();
        } catch (DataAccessException e) {
            throw new RuntimeException("Database setup failed: " + e.getMessage(), e);
        }
    }

    private void initializeTable() throws DataAccessException {
        String createSQL = """
                CREATE TABLE IF NOT EXISTS auth (
                    username VARCHAR(255) NOT NULL,
                    authToken VARCHAR(255) NOT NULL,
                    PRIMARY KEY (authToken)
                )
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(createSQL)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create auth table", e);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String insertSQL = "INSERT INTO auth (username, authToken) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            stmt.setString(1, auth.username());
            stmt.setString(2, auth.authToken());
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Could not insert auth token", e);
        }
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        String selectSQL = "SELECT username FROM auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
            stmt.setString(1, token);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    String username = result.getString("username");
                    return new AuthData(username, token);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch auth token: " + token, e);
        }
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        String deleteSQL = "DELETE FROM auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to remove auth token: " + token, e);
        }
    }

    @Override
    public void clear() {
        String clearSQL = "TRUNCATE TABLE auth";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(clearSQL)) {
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Unable to clear auth table: " + e.getMessage(), e);
        }
    }
}
