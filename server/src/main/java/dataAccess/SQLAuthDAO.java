package dataaccess;
import model.AuthData;

import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {
    public SQLAuthDAO() {
        try {DatabaseManager.createDatabase(); } catch (DataAccessException exception) {
            throw new RuntimeException(exception)
        }
        try (var conn = DatabaseManager.getConnection()) {
            var createTestTable = """
                    )""";
            try (var createTableStatement = conn.prepareStatement(createTestTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void addAuth(AuthData authData) {
        try (var conn = DatabaseManager.getConnection()) {
            try var statement = conn.prepareStatement("Insert into auth ")}
        }
    }
}
