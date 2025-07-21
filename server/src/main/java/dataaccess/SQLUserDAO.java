package dataaccess;

import model.UserData;
import java.sql.SQLException;
import java.sql.Connection;
import org.mindrot.jbcrypt.BCrypt;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException ex) {
            throw new RuntimeException("Failed to create database: " + ex.getMessage(), ex);
        }

        try (var conn = DatabaseManager.getConnection()) {
            String createUserTableSQL = """
                    CREATE TABLE IF NOT EXISTS user (
                        username VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        email VARCHAR(255),
                        PRIMARY KEY (username)
                        )""";

            try (var stmt = conn.prepareStatement(createUserTableSQL)) {
                stmt.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error initializing user table: " + e.getMessage(), e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String selectSQL = "SELECT username, password, email FROM user WHERE username=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(selectSQL)) {

            stmt.setString(1, username);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var password = rs.getString("password");
                    var email = rs.getString("email");
                    return new UserData(username, password, email);
                } else {
                    throw new DataAccessException("User not found: " + username);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching user data: " + username);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String insertSQL = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(insertSQL)) {

            stmt.setString(1, user.username());
            stmt.setString(2, hashPassword(user.password()));
            stmt.setString(3, user.email());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user: " + user.username());
        }
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        UserData user = getUser(username);
        return passwordMatches(password, user.password());
    }

    @Override
    public void clear() {
        String truncateSQL = "TRUNCATE user";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(truncateSQL)) {

            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error clearing user table: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean passwordMatches(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}
