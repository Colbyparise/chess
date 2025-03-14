package dataaccess;
import model.AuthData;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException ex) {
            throw new RuntimeException("Error creating database: " + ex.getMessage(), ex);
        }

        try (var conn = DatabaseManager.getConnection()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS auth (" +
                    "username VARCHAR(255) NOT NULL, " +
                    "authToken VARCHAR(255) NOT NULL, " +
                    "PRIMARY KEY (authToken))";

            try (var stmt = conn.prepareStatement(createTableSQL)) {
                stmt.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error setting up auth table: " + e.getMessage(), e);
        }
    }

    @Override
    public void addAuth(AuthData authData) {
        String insertSQL = "INSERT INTO auth (username, authToken) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(insertSQL)) {

            stmt.setString(1, authData.username());
            stmt.setString(2, authData.authToken());
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
        }
    }

    @Override
    public void deleteAuth(String authToken) {
        String deleteSQL = "DELETE FROM auth WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(deleteSQL)) {

            stmt.setString(1, authToken);
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {

        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String selectSQL = "SELECT username, authToken FROM auth WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(selectSQL)) {

            stmt.setString(1, authToken);
            try (var resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    var username = resultSet.getString("username");
                    return new AuthData(username, authToken);
                } else {
                    throw new DataAccessException("Auth Token does not exist: " + authToken);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving Auth Token: " + authToken);
        }
    }

    @Override
    public void clear() {
        String truncateSQL = "TRUNCATE auth";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(truncateSQL)) {
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error clearing the auth table: " + e.getMessage());
        }
    }
}