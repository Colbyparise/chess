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
                    "PRIMARY KEY (authToken)";

            try (var statement = connection.prepareStatement(createTableSQL)) {
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException exception) {
            throw new RuntimeException("Error setting up auth table: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void createAuth(AuthData authData) {

    }

    @Override
    public void deleteAuth(String authToken) {

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void clear() {

    }
}
