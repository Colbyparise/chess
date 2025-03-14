package server;

import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.SQLUserDAO;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SQLUserDAOTest {

    private UserDAO dao;
    private UserData defaultUser;

    @BeforeEach
    void setUp() throws DataAccessException, SQLException {
        DatabaseManager.createDatabase();
        dao = new SQLUserDAO();
        clearUserTable();
        defaultUser = new UserData("username", "password", "email");
    }

    @AfterEach
    void tearDown() throws SQLException, DataAccessException {
        clearUserTable();
    }

    private void clearUserTable() throws SQLException, DataAccessException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("TRUNCATE user")) {
            statement.executeUpdate();
        }
    }

    @Test
    void createUserPositive() throws DataAccessException, SQLException {
        dao.createUser(defaultUser);

        UserData resultUser = fetchUserFromDatabase(defaultUser.username());

        assertEquals(defaultUser.username(), resultUser.username());
        assertTrue(passwordMatches(defaultUser.password(), resultUser.password()));
        assertEquals(defaultUser.email(), resultUser.email());
    }

    @Test
    void getUserPositive() throws DataAccessException {
        dao.createUser(defaultUser);

        UserData resultUser = dao.getUser(defaultUser.username());

        assertEquals(defaultUser.username(), resultUser.username());
        assertTrue(passwordMatches(defaultUser.password(), resultUser.password()));
        assertEquals(defaultUser.email(), resultUser.email());
    }

    @Test
    void getUserNegative() {
        assertThrows(DataAccessException.class, () -> dao.getUser(defaultUser.username()));
    }

    @Test
    void authenticateUserPositive() throws DataAccessException {
        dao.createUser(defaultUser);
        assertTrue(dao.authenticateUser(defaultUser.username(), defaultUser.password()));
    }

    @Test
    void authenticateUserNegative() throws DataAccessException {
        dao.createUser(defaultUser);
        assertFalse(dao.authenticateUser(defaultUser.username(), "badPass"));
    }

    @Test
    void clear() throws DataAccessException, SQLException {
        dao.createUser(defaultUser);
        dao.clear();

        assertFalse(userExistsInDatabase(defaultUser.username()));
    }

    private boolean passwordMatches(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    private UserData fetchUserFromDatabase(String username) throws SQLException, DataAccessException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT username, password, email FROM user WHERE username=?")) {
            statement.setString(1, username);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return new UserData(results.getString("username"), results.getString("password"), results.getString("email"));
                }
                return null;
            }
        }
    }

    private boolean userExistsInDatabase(String username) throws SQLException, DataAccessException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT username FROM user WHERE username=?")) {
            statement.setString(1, username);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }
}