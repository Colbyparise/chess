package server;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.SQLAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SQLAuthDAOTest {

    AuthDAO dao;

    AuthData defaultAuth;

    @BeforeEach
    void setUp() throws DataAccessException, SQLException {
        DatabaseManager.createDatabase();
        dao = new SQLAuthDAO();
        clearAuthTable();
        defaultAuth = new AuthData("username", "token");
    }

    @AfterEach
    void tearDown() throws SQLException, DataAccessException {
        clearAuthTable();
    }

    private void clearAuthTable() throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement("TRUNCATE auth")) {
            statement.executeUpdate();
        }
    }

    private boolean authEntryExists(String username) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT username FROM auth WHERE username=?")) {
            statement.setString(1, username);
            try (var results = statement.executeQuery()) {
                return results.next();
            }
        }
    }


    @Test
    void addAuthPositive() throws DataAccessException, SQLException {
        dao.addAuth(defaultAuth);
        assertTrue(authEntryExists(defaultAuth.username()));
        AuthData result = dao.getAuth(defaultAuth.authToken());
        assertEquals(defaultAuth, result);
    }


    @Test
    void addAuthNegative() throws DataAccessException, SQLException {
        dao.addAuth(defaultAuth);
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM auth WHERE username=?")) {
            statement.setString(1, defaultAuth.username());
            try (var results = statement.executeQuery()) {
                results.next();
                int count = results.getInt(1);
                assertEquals(1, count); // Assert that only one row exists
            }
        }
    }

    @Test
    void deleteAuthPositive() throws DataAccessException, SQLException {
        dao.addAuth(defaultAuth);
        dao.deleteAuth(defaultAuth.authToken());
        assertFalse(authEntryExists(defaultAuth.username()));
    }

    @Test
    void deleteAuthNegative() throws DataAccessException, SQLException {
        assertDoesNotThrow(() -> dao.deleteAuth("badToken"));
    }

    @Test
    void getAuthPositive() throws DataAccessException {
        dao.addAuth(defaultAuth);
        AuthData result = dao.getAuth(defaultAuth.authToken());
        assertEquals(defaultAuth, result);
    }

    @Test
    void getAuthNegative() {
        dao.addAuth(defaultAuth);
        assertThrows(DataAccessException.class, () -> dao.getAuth("badToken"));
    }

    @Test
    void clear() throws DataAccessException, SQLException {
        dao.addAuth(defaultAuth);
        dao.clear();
        assertFalse(authEntryExists(defaultAuth.username()));
    }
}