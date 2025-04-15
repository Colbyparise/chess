package client;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import network.http.GameRequest;
import network.http.JoinGame;
import network.http.GetGames;
import network.http.LoginResult;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import network.ResponseException;
import server.Server;
import facade.ServerFacade;

import java.sql.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    AuthDAO authDAO;
    GameDAO gameDAO;
    UserDAO userDAO;


    @BeforeAll
    public static void init() throws DataAccessException {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade("http://localhost:" + port);
        System.out.println("Started test HTTP server on " + port);

    }

    @BeforeEach
    public void prep() throws DataAccessException {
        Connection conn = DatabaseManager.getConnection();
        authDAO = new SQLAuthDAO(conn);
        gameDAO = new SQLGameDAO(conn);
        userDAO = new SQLUserDAO(conn);

        authDAO.clear();
        userDAO.clear();
        gameDAO.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void testRegisterValid() {
        var req = new UserData("kal", "storms", "roshar.com");
        Assertions.assertDoesNotThrow(() -> facade.register(req));
    }

    @Test
    public void testRegisterInvalid() {
        var req = new UserData("kal", "storms", "roshar.com");
        Assertions.assertDoesNotThrow(() -> facade.register(req));
        try {
            facade.register(req);
            Assertions.fail("Did not throw an error fro duplicate registration");
        } catch (ResponseException e) {
            Assertions.assertEquals(403, e.statusCode());
        }
    }

    @Test
    public void testLoginValid() throws DataAccessException {
        var expectedResult = new LoginResult("kal", "a");
        var req = new UserData("kal", "storms", "roshar.com");
        var userData = new UserData(req.username(), BCrypt.hashpw(req.password(), BCrypt.gensalt()), req.email());

        userDAO.createUser(userData);

        var ref = new Object() {
            LoginResult actualResult;
        };
        Assertions.assertDoesNotThrow(() -> {
            ref.actualResult = facade.login(req);
        });
        Assertions.assertEquals(ref.actualResult.username(), expectedResult.username());
    }


    @Test
    public void testLoginInvalid() throws DataAccessException {
        var req = new UserData("kal", "storms", "roshar.com");
        var userData = new UserData(req.username(), BCrypt.hashpw(req.password() + "MM", BCrypt.gensalt()), req.email());
        userDAO.createUser(userData);

        try {
            facade.login(req);
            Assertions.fail("Did not throw an unauthorized error");
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.statusCode());
        }
    }

    @Test
    public void testLogoutValid() throws DataAccessException {
        var auth = new AuthData("abc123", "heraldOfWind");
        authDAO.createAuth(auth);
        Assertions.assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    public void testLogoutInvalid() {
        try {
            facade.logout("e");
            Assertions.fail("Did not throw an unauthorized error");
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.statusCode());
        }
    }

    @Test
    public void testListValid() throws DataAccessException, ResponseException {
        var auth = new AuthData("abc123", "heraldOfWind");

        authDAO.createAuth(auth);
        Assertions.assertDoesNotThrow(() -> facade.listGames(new GetGames(auth.authToken())));
    }

    @Test
    public void testListInvalid() throws DataAccessException {
        try {
            facade.listGames(new GetGames("e"));
            Assertions.fail("Did not throw an unauthorized error");
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.statusCode());
        }
    }

    @Test
    public void createGameValid() throws DataAccessException, ResponseException {
        var auth = new AuthData("abc123", "heraldOfWind");
        authDAO.createAuth(auth);
        Assertions.assertDoesNotThrow(() -> facade.createGame(new GameRequest(auth.authToken(), "test")));

    }

    @Test
    public void createGameInvalid() {
        try {
            facade.createGame(new GameRequest("e", "test"));
            Assertions.fail("Did not throw an unauthorized error");
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.statusCode());
        }
    }

    @Test
    public void joinGameValid() throws DataAccessException, ResponseException {
        var auth = new AuthData("abc123", "heraldOfWind");
        var game = new GameData(1, null, "syl", "braize", new ChessGame());

        authDAO.createAuth(auth);
        gameDAO.createGame(auth, "braize");
        gameDAO.updateGame(game);

        Assertions.assertDoesNotThrow(() -> facade.joinGame(new JoinGame(auth.authToken(), "WHITE", game.gameID())));
    }

    @Test
    public void joinGameInvalidUnauthorized() {
        try {
            facade.joinGame(new JoinGame("e", "WHITE", 1));
            Assertions.fail("Did not throw an unauthorized error");
        } catch (ResponseException e) {
            Assertions.assertEquals(401, e.statusCode());
        }
    }

    @Test
    public void joinGameInvalidTaken() throws DataAccessException {
        var auth = new AuthData("abc123", "heraldOfWind");
        var game = new GameData(1, "Taln", "syl", "braize", new ChessGame());
        authDAO.createAuth(auth);
        gameDAO.createGame(auth, "braize");
        gameDAO.updateGame(game);


        try {
            facade.joinGame(new JoinGame(auth.authToken(), "WHITE", game.gameID()));
            Assertions.fail("Did not throw a taken error");
        } catch (ResponseException e) {
            Assertions.assertEquals(403, e.statusCode());
        }
    }

}