package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SQLAuthDAOTests {

    private SQLAuthDAO authDAO;
    private final AuthData testAuth = new AuthData("testUser", "testToken");

    @BeforeEach
    void setUp() {
        authDAO = new SQLAuthDAO();
        authDAO.clear(); // clear table before each test
    }

    @Test
    void createAuthPositive() throws DataAccessException {
        authDAO.createAuth(testAuth);
        AuthData result = authDAO.getAuth("testToken");
        assertNotNull(result);
        assertEquals("testUser", result.username());
    }

    @Test
    void createAuthNegative() throws DataAccessException {
        authDAO.createAuth(testAuth);
        AuthData duplicate = new AuthData("anotherUser", "testToken"); // same token
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(duplicate));
    }

    @Test
    void getAuthPositive() throws DataAccessException {
        authDAO.createAuth(testAuth);
        AuthData result = authDAO.getAuth("testToken");
        assertNotNull(result);
        assertEquals("testUser", result.username());
    }

    @Test
    void getAuthNegative() throws DataAccessException {
        AuthData result = authDAO.getAuth("nonexistent");
        assertNull(result);  // not found should return null
    }

    @Test
    void deleteAuthPositive() throws DataAccessException {
        authDAO.createAuth(testAuth);
        authDAO.deleteAuth("testToken");
        assertNull(authDAO.getAuth("testToken"));
    }

    @Test
    void deleteAuthNegative() {
        assertDoesNotThrow(() -> authDAO.deleteAuth("nope"));
    }

    @Test
    void clearPositive() throws DataAccessException {
        authDAO.createAuth(testAuth);
        assertNotNull(authDAO.getAuth("testToken"));
        authDAO.clear();
        assertNull(authDAO.getAuth("testToken"));
    }
}
