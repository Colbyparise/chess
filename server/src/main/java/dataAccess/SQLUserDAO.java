package dataaccess;

import dataaccess.interfaces.UserDAO;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {

    private final Connection dbConn;
    private final String userTable;

    public SQLUserDAO(Connection dbConnection) {
        this.dbConn = dbConnection;
        this.userTable = DatabaseManager.TABLES[DatabaseManager.TableName.Users.ordinal()];
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        final String query = "SELECT username, password, email FROM " + userTable + " WHERE username = ?;";
        try (PreparedStatement statement = dbConn.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new UserData(
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("email")
                );
            }
        } catch (SQLException sqlEx) {
            throw new DataAccessException("Error retrieving user: " + sqlEx.getMessage());
        }
        return null;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        final String insertQuery = "INSERT INTO " + userTable + " (username, password, email) VALUES (?, ?, ?);";
        try (PreparedStatement statement = dbConn.prepareStatement(insertQuery)) {
            statement.setString(1, user.username());
            statement.setString(2, user.password());
            statement.setString(3, user.email());
            statement.executeUpdate();
        } catch (SQLException sqlEx) {
            throw new DataAccessException("Failed to insert user: " + sqlEx.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        ClearUtil.clearTable(userTable, dbConn);
    }
}
