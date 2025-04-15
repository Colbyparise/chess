package server;

import dataaccess.*;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import handler.*;
import com.google.gson.Gson;
import network.MessageError;
import server.websocket.WebSocketHandler;
import spark.*;

import java.sql.Connection;


public class Server {

    private static interface RequestPredicate {
        String handle(Request req, Response res) throws DataAccessException;
    }

    private final WebSocketHandler webSocketHandler;

    private ClearHandler clearHandler;
    private RegisterHandler registerHandler;
    private LoginHandler loginHandler;
    private LogoutHandler logoutHandler;

    private ListGamesHandler listGamesHandler;
    private JoinGameHandler joinGameHandler;
    private CreateGameHandler createGameHandler;

    private AuthDAO authDAO;
    private UserDAO userDAO;
    private GameDAO gameDAO;
    Connection connection;

    private Gson gson;

    public Server() {

        try {
            DatabaseManager.createDatabase();
            connection = DatabaseManager.getConnection();

            authDAO = new SQLAuthDAO(connection);
            userDAO = new SQLUserDAO(connection);
            gameDAO = new SQLGameDAO(connection);

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        webSocketHandler = new WebSocketHandler(authDAO, gameDAO);


        clearHandler = new ClearHandler(authDAO, userDAO, gameDAO);
        registerHandler = new RegisterHandler(userDAO, authDAO);
        loginHandler = new LoginHandler(userDAO, authDAO);
        logoutHandler = new LogoutHandler(authDAO);

        listGamesHandler = new ListGamesHandler(authDAO, gameDAO);
        joinGameHandler = new JoinGameHandler(authDAO, gameDAO);
        createGameHandler = new CreateGameHandler(authDAO, gameDAO);

        gson = new Gson();
    }

    public int run(int desiredPort) {

        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.webSocket("/ws", webSocketHandler);

        // Register your endpoints and handle exceptions here.
        registerEndpoints();

        //This line initializes the server and can be removed once you have a functioning endpoint
        //Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void registerEndpoints() {
        Spark.post("/user", (req, res) -> handleRequest(req, res, (reqIn, resIn) -> registerHandler.register(reqIn, resIn, gson)));
        Spark.post("/session", (req, res) -> handleRequest(req, res, (reqIn, resIn) -> loginHandler.login(reqIn, resIn, gson)));
        Spark.delete("/session", (req, res) -> handleRequest(req, res, (reqIn, resIn) -> logoutHandler.logout(reqIn, resIn)));

        Spark.get("/game", (req, res) -> handleRequest(req, res, (reqIn, resIn) -> listGamesHandler.listGames(reqIn, resIn, gson)));
        Spark.post("/game", (req, res) -> handleRequest(req, res, (reqIn, resIn) -> createGameHandler.createGame(reqIn, resIn, gson)));
        Spark.put("/game", (req, res) -> handleRequest(req, res, (reqIn, resIn) -> joinGameHandler.joinGame(reqIn, resIn, gson)));

        Spark.delete("/db", (req, res) -> handleRequest(req, res, (reqIn, resIn) -> clearHandler.clear(reqIn, resIn)));
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object handleRequest(Request req, Response res, RequestPredicate predicate) {
        try {
            return predicate.handle(req, res);
        } catch (BadRequestException ex) {
            res.status(400);
            var error = new MessageError(ex.getMessage());
            return gson.toJson(error);
        } catch (UnauthorizedException ex) {
            res.status(401);
            var error = new MessageError(ex.getMessage());
            return gson.toJson(error);
        } catch (TakenException ex) {
            res.status(403);
            var error = new MessageError(ex.getMessage());
            return gson.toJson(error);
        } catch (DataAccessException ex) {
            res.status(500);
            var error = new MessageError(ex.getMessage());
            return gson.toJson(error);
        }
    }
}