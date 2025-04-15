package dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClearUtil {

    /**
     * Clears the contents of a specific table in the database by truncating it.
     *
     * @param tableName  the name of the table to clear.
     * @param connection the connection to the database.
     * @throws DataAccessException if an error occurs while accessing the database.
     */
    public static void clearTable(String tableName, Connection connection) throws DataAccessException {
        String truncateQuery = String.format("TRUNCATE TABLE %s;", tableName);

        try (PreparedStatement stmt = connection.prepareStatement(truncateQuery)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing table: " + e.getMessage());
        }
    }
}
