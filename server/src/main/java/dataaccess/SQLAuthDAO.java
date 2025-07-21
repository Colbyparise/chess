package dataaccess;
import model.AuthData;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {
    public SQLAuthDAO() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException exception) {
            throw new RuntimeException("Error creating database: " + exception.getMessage(), exception);
        }

        try (var connection = DatabaseManager.getConnection()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS auth (" +
                    "username VARCHAR(255) NOT NULL, " +
                    "authToken VARCHAR(255) NOT NULL, " +
                    "PRIMARY KEY (authToken))";

            try (var statement = connection.prepareStatement(createTableSQL)) {
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException exception) {
            throw new RuntimeException("Error setting up auth table: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        String insertSQL = "INSERT INTO auth (username, authToken) VALUES (?, ?)";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(insertSQL)) {

            statement.setString(1, authData.username());
            statement.setString(2, authData.authToken());
            statement.executeUpdate();
        } catch (SQLException | DataAccessException exception) {
            throw new DataAccessException("Error creating auth token", exception);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String deleteSQL = "DELETE FROM auth WHERE authToken=?";
        try (var connection = DatabaseManager.getConnection();
            var statement = connection.prepareStatement(deleteSQL)) {
            statement.setString(1, authToken);
            statement.executeUpdate();
        } catch (SQLException | DataAccessException exception) {
            throw new DataAccessException("Error deleting auth token", exception);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String selectSQL = "SELECT username FROM auth WHERE authToken=?";
        try (var connection = DatabaseManager.getConnection();
            var statement = connection.prepareStatement(selectSQL)) {
            statement.setString(1, authToken);
            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new AuthData(resultSet.getString("username"), authToken);
                } else {
                    return null;
                }
            }

        } catch (SQLException exception) {
            throw new DataAccessException("Error retrieving Auth Token: " + authToken, exception);
        }

    }

    @Override
    public void clear() {
        String truncateSQL = "DELETE FROM auth";
        try (var connection = DatabaseManager.getConnection();
            var statement = connection.prepareStatement(truncateSQL)) {
            statement.executeUpdate();
        } catch (SQLException | DataAccessException exception) {
            throw new RuntimeException("Error clearing the auth table: " + exception.getMessage());
        }

    }
}
