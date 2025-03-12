package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException ex) {
            throw new RuntimeException("Failed to create database", ex);
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String createTableQuery = """
                CREATE TABLE IF NOT EXISTS user (
                    username VARCHAR(255) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255),
                    PRIMARY KEY (username)
                )""";
            try (PreparedStatement stmt = conn.prepareStatement(createTableQuery)) {
                stmt.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Failed to create user table", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String query = "SELECT username, password, email FROM user WHERE username=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, username);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    String password = results.getString("password");
                    String email = results.getString("email");
                    return new UserData(username, password, email);
                } else {
                    throw new DataAccessException("User not found: " + username);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while fetching user: " + username);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String insertQuery = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(insertQuery)) {

            statement.setString(1, user.username());
            statement.setString(2, hashPassword(user.password()));
            statement.setString(3, user.email());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("User already exists or database error: " + user.username());
        }
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        String query = "SELECT password FROM user WHERE username=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, username);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    String storedPassword = results.getString("password");
                    return verifyPassword(password, storedPassword);
                }
                return false;  // User not found
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error during authentication for user: " + username);
        }
    }

    @Override
    public void clear() {
        String query = "DELETE FROM user";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Failed to clear user table", e);
        }
    }

    // Hashes the password using BCrypt
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Verifies the password against the stored hash
    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}
