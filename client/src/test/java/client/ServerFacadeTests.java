package client;

import org.junit.jupiter.api.*;
import server.Server;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server testServer;
    private ServerFacade client;
    private static int serverPort;

    @BeforeAll
    public static void startServer() {
        testServer = new Server();
        serverPort = testServer.run(0);
        System.out.println("Test server started on port " + serverPort);
    }

    @AfterAll
    public static void shutdownServer() {
        testServer.stop();
    }

    @BeforeEach
    public void resetBeforeTest() throws Exception {
        testServer.clearDB();
        client = new ServerFacade();
    }

    @AfterEach
    public void resetAfterTest() {
        testServer.clearDB();
    }

    @Test
    public void testSuccessfulRegistration() {
        assertTrue(client.register("alice", "pass123", "alice@email.com"));
    }

    @Test
    public void testDuplicateRegistrationFails() {
        client.register("bob", "secret", "bob@email.com");
        assertFalse(client.register("bob", "secret", "bob@email.com"));
    }

    @Test
    public void testLoginWithCorrectCredentials() {
        client.register("carol", "secure", "carol@email.com");
        assertTrue(client.login("carol", "secure"));
    }

    @Test
    public void testLoginWithIncorrectPassword() {
        client.register("dave", "mypass", "dave@email.com");
        assertFalse(client.login("dave", "wrongpass"));
    }

    @Test
    public void testLogoutWhenLoggedIn() {
        client.register("eve", "wordpass", "eve@email.com");
        assertTrue(client.logout());
    }

    @Test
    public void testLogoutWithoutLogin() {
        assertFalse(client.logout());
    }

    @Test
    public void testCreateGameWhenAuthenticated() {
        client.register("frank", "pw", "frank@email.com");
        int gameId = client.createGame("Chess Match");
        assertTrue(gameId >= 0);
    }

    @Test
    public void testCreateGameWithoutAuthentication() {
        assertEquals(-1, client.createGame("Unauthorized Game"));
    }

    @Test
    public void testListGamesWithGamesPresent() {
        client.register("grace", "pass", "grace@email.com");
        client.createGame("Epic Battle");
        assertEquals(1, client.listGames().size());
    }

    @Test
    public void testListGamesWithNoGames() {
        assertEquals(HashSet.newHashSet(8), client.listGames());
    }

    @Test
    public void testJoinGameSuccessfully() {
        client.register("harry", "pass", "harry@email.com");
        int gameId = client.createGame("Classic Game");
        assertTrue(client.joinGame(gameId, "WHITE"));
    }

    @Test
    public void testJoinGameWithTakenColor() {
        client.register("ian", "pw", "ian@email.com");
        int gameId = client.createGame("Reserved Game");
        client.joinGame(gameId, "WHITE");
        assertFalse(client.joinGame(gameId, "WHITE"));
    }
}
