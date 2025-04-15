package server;

import com.google.gson.Gson;
import dataaccess.*;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import handler.*;
import network.MessageError;
import server.websocket.WebSocketHandler;
import spark.*;

import java.sql.Connection;

public class Server {

    // Functional interface for handling Spark requests with data access
    @FunctionalInterface
    private interface RequestHandler {
        String handle(Request req, Response res) throws DataAccessException;
    }

    private final WebSocketHandler socketHandler;

    // HTTP request handlers
    private final ClearHandler clearHandler;
    private final RegisterHandler registerHandler;
    private final LoginHandler loginHandler;
    private final LogoutHandler logoutHandler;
    private final ListGamesHandler listGamesHandler;
    private final JoinGameHandler joinGameHandler;
    private final CreateGameHandler createGameHandler;

    // DAOs
    private final AuthDAO authDAO;
    private final UserDAO userDAO;
    private final GameDAO gameDAO;

    private final Gson gson = new Gson();

    private final Connection connection;

    public Server() {
        try {
            DatabaseManager.createDatabase();
            this.connection = DatabaseManager.getConnection();

            this.authDAO = new SQLAuthDAO(connection);
            this.userDAO = new SQLUserDAO(connection);
            this.gameDAO = new SQLGameDAO(connection);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to initialize database connection", e);
        }

        this.socketHandler = new WebSocketHandler(authDAO, gameDAO);

        // Initialize handlers
        this.clearHandler = new ClearHandler(authDAO, userDAO, gameDAO);
        this.registerHandler = new RegisterHandler(userDAO, authDAO);
        this.loginHandler = new LoginHandler(userDAO, authDAO);
        this.logoutHandler = new LogoutHandler(authDAO);
        this.listGamesHandler = new ListGamesHandler(authDAO, gameDAO);
        this.joinGameHandler = new JoinGameHandler(authDAO, gameDAO);
        this.createGameHandler = new CreateGameHandler(authDAO, gameDAO);
    }

    public int run(int port) {
        Spark.port(port);
        Spark.staticFiles.location("web");
        Spark.webSocket("/ws", socketHandler);

        registerRoutes();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void registerRoutes() {
        Spark.post("/user", (req, res) -> handle(req, res, (r, s) -> registerHandler.register(r, s, gson)));
        Spark.post("/session", (req, res) -> handle(req, res, (r, s) -> loginHandler.login(r, s, gson)));
        Spark.delete("/session", (req, res) -> handle(req, res, logoutHandler::logout));

        Spark.get("/game", (req, res) -> handle(req, res, (r, s) -> listGamesHandler.listGames(r, s, gson)));
        Spark.post("/game", (req, res) -> handle(req, res, (r, s) -> createGameHandler.createGame(r, s, gson)));
        Spark.put("/game", (req, res) -> handle(req, res, (r, s) -> joinGameHandler.joinGame(r, s, gson)));

        Spark.delete("/db", (req, res) -> handle(req, res, clearHandler::clear));
    }

    private Object handle(Request req, Response res, RequestHandler handler) {
        try {
            return handler.handle(req, res);
        } catch (BadRequestException e) {
            res.status(400);
            return gson.toJson(new MessageError(e.getMessage()));
        } catch (UnauthorizedException e) {
            res.status(401);
            return gson.toJson(new MessageError(e.getMessage()));
        } catch (TakenException e) {
            res.status(403);
            return gson.toJson(new MessageError(e.getMessage()));
        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(new MessageError(e.getMessage()));
        }
    }
}
