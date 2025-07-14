package passoff.server;

import service.GameService;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


class GameServiceTests {

    private GameService gameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    private String validToken;

    @BeforeEach
    void setUp() throws DataAccessException {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);

        // Add a user manually for testing
        AuthData auth = new AuthData("testuser", "valid-token");
        authDAO.createAuth(auth);
        validToken = auth.authToken();
    }

    @Test
    void positiveListGames() throws DataAccessException {
        var games = gameService.listGames(validToken);
        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    void negativeListGames() {
        assertThrows(DataAccessException.class, () -> gameService.listGames("bad-token"));
    }

    @Test
    void positiveCreateGame() throws DataAccessException {
        int gameId = gameService.createGame(validToken, "Chess Game");
        assertTrue(gameId > 0);
    }

    @Test
    void negativeCreateGame() {
        assertThrows(DataAccessException.class, () -> gameService.createGame(validToken, ""));
    }

    @Test
    void possitiveJoinGame() throws DataAccessException {
        int gameId = gameService.createGame(validToken, "Chess Game");
        boolean joined = gameService.joinGame(validToken, gameId, "WHITE");

        assertTrue(joined);
    }

    @Test
    void negativeJoinGame() throws DataAccessException {
        int gameId = gameService.createGame(validToken, "Chess Game");

        assertThrows(DataAccessException.class, () -> gameService.joinGame(validToken, gameId, "BLUE"));
    }

    @Test
    void clear() throws DataAccessException {
        gameService.createGame(validToken, "Chess Game");
        gameService.clear();

        assertTrue(gameService.listGames(validToken).isEmpty());
    }
}
