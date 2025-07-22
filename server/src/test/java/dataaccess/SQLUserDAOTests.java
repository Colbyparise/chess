package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTests {

    private SQLUserDAO dao;

    @BeforeEach
    void setUp() {
        dao = new SQLUserDAO();
        dao.clear(); // Reset DB before each test
    }

    @Test
    void createUserPositive() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@example.com");
        dao.createUser(user);

        UserData fetched = dao.getUser("alice");

        assertEquals("alice", fetched.username());
        assertEquals("alice@example.com", fetched.email());
        assertNotEquals("password123", fetched.password(), "Password should be hashed");
    }

    @Test
    void createUserDuplicateUsernameThrows() throws DataAccessException {
        UserData user = new UserData("bob", "password123", "bob@example.com");
        dao.createUser(user);

        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                dao.createUser(user)
        );
        assertTrue(ex.getMessage().contains("Error creating user"));
    }

    @Test
    void getUserNonExistentThrows() {
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                dao.getUser("ghost")
        );
        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void authenticateUserCorrectPassword() throws DataAccessException {
        UserData user = new UserData("carol", "mySecret", "carol@example.com");
        dao.createUser(user);

        boolean result = dao.authenticateUser("carol", "mySecret");
        assertTrue(result);
    }

    @Test
    void authenticateUserIncorrectPassword() throws DataAccessException {
        UserData user = new UserData("dan", "correctPassword", "dan@example.com");
        dao.createUser(user);

        boolean result = dao.authenticateUser("dan", "wrongPassword");
        assertFalse(result);
    }

    @Test
    void clearRemovesAllUsers() throws DataAccessException {
        UserData user = new UserData("eve", "1234", "eve@example.com");
        dao.createUser(user);

        dao.clear();

        assertThrows(DataAccessException.class, () ->
                dao.getUser("eve")
        );
    }
}
