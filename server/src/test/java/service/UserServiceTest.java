package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    void positiveRegistration() throws DataAccessException {
        UserData user = new UserData("alice", "pass", "alice@example.com");
        AuthData auth = userService.createUser(user);

        assertNotNull(auth);
        assertEquals("alice", auth.username());
    }

    @Test
    void negativeRegistration() throws DataAccessException {
        UserData user = new UserData("bob", "pass", "bob@example.com");
        userService.createUser(user);

        UserData duplicate = new UserData("bob", "pass", "bob@example.com");

        assertThrows(DataAccessException.class, () -> userService.createUser(duplicate));
    }

    @Test
    void positiveLogin() throws DataAccessException {
        UserData user = new UserData("charlie", "pass", "charlie@example.com");
        userService.createUser(user);

        AuthData auth = userService.loginUser(user);
        assertEquals("charlie", auth.username());
    }

    @Test
    void negativeLogin() {
        UserData user = new UserData("dave", "pass", "dave@example.com");
        assertThrows(DataAccessException.class, () -> userService.loginUser(user));
    }

    @Test
    void positiveLogout() throws DataAccessException {
        UserData user = new UserData("eve", "pass", "eve@example.com");
        AuthData auth = userService.createUser(user);

        assertDoesNotThrow(() -> userService.logoutUser(auth.authToken()));
    }

    @Test
    void negativeLogout() {
        assertThrows(DataAccessException.class, () -> userService.logoutUser("bad-token"));
    }

    @Test
    void clear() throws DataAccessException {
        UserData user = new UserData("frank", "pass", "frank@example.com");
        userService.createUser(user);
        userService.clear();

        assertThrows(DataAccessException.class, () -> userService.loginUser(user));
    }
}
