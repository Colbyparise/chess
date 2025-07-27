package client;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import model.UserData;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    public static ServerFacade facade;
    public static AuthData auth;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }
    @BeforeEach
    void clearDB() throws Exception {
        facade.clearDB();
        try {
            facade.logout(auth.authToken());
        } catch (Exception ignored) {
        }
        auth = facade.register(new UserData("testuser", "password", "test@example.com"));
    }

    @Test
    void registerPositive() throws Exception {
        var newUser = new UserData("player1", "password", "p1@email.com");
        var newAuth = facade.register(newUser);
        assertNotNull(newAuth);
        assertTrue(newAuth.authToken().length() > 10);
        assertEquals("player1", newAuth.username());
    }

    @Test
    void registerNegative() {
        var duplicateUser = new UserData("testuser", "password", "test@example.com");
        assertThrows(Exception.class, () -> facade.register(duplicateUser));
    }

    @Test
    void loginPositive() throws Exception {
        var result = facade.login("testuser", "password");
        assertNotNull(result);
        assertEquals("testuser", result.username());
    }

    @Test
    void loginNegative() {
        assertThrows(Exception.class, () -> facade.login("invalid", "wrong"));
    }

    @Test
    void logoutPositive() {
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    void logoutNegative() {
        assertThrows(Exception.class, () -> facade.logout("badtoken"));
    }

    @Test
    void createGamePositive() {
        int gameId = facade.createGame("Cool Game", auth.authToken());
        assertTrue(gameId > 0);
    }

    @Test
    void createGameNegative() {
        int gameId = facade.createGame("Cool Game", "invalidtoken");
        assertEquals(-1, gameId);
    }

    @Test
    void listGamesPositive() {
        facade.createGame("Game A", auth.authToken());
        HashSet<GameData> games = facade.listGames(auth.authToken());
        assertNotNull(games);
        assertFalse(games.isEmpty());
    }

    @Test
    void listGamesNegative() {
        HashSet<GameData> games = facade.listGames("invalidtoken");
        assertTrue(games.isEmpty());
    }

    @Test
    void joinGamePositive() throws Exception {
        int gameId = facade.createGame("Chess Duel", auth.authToken());
        boolean joined = facade.joinGame(gameId, ChessGame.TeamColor.WHITE, auth.authToken());
        assertTrue(joined);
    }

    @Test
    void joinGameNegative() {
        assertThrows(Exception.class, () -> facade.joinGame(-1, ChessGame.TeamColor.BLACK, auth.authToken()));
    }

    @Test
    void observeGamePositive() {
        int gameId = facade.createGame("Watchable Game", auth.authToken());
        assertDoesNotThrow(() -> facade.observeGame(gameId, auth.authToken()));
    }

    @Test
    void observeGameNegative() {
        assertThrows(Exception.class, () -> facade.observeGame(-999, auth.authToken()));
    }
}
