package server;

import dataaccess.*;
import org.eclipse.jetty.websocket.api.Session;
import service.GameService;
import service.UserService;
import spark.*;
import model.GameData;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    // DAOs
    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    // Services
    public static UserService userService;
    public static GameService gameService;

    // Handlers
    UserHandler userHandler;
    GameHandler gameHandler;

    // WebSocket Session Map: {Session: gameID}
    public static ConcurrentHashMap<Session, Integer> gameSessions = new ConcurrentHashMap<>();

    // ✅ In-memory live game cache: {gameID: GameData}
    public static Map<Integer, GameData> liveGames = new ConcurrentHashMap<>();

    public Server() {
        userDAO = new SQLUserDAO();
        authDAO = new SQLAuthDAO();
        gameDAO = new SQLGameDAO();

        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        userHandler = new UserHandler(userService);
        gameHandler = new GameHandler(gameService);

        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // WebSocket endpoint
        Spark.webSocket("/connect", WebsocketHandler.class);

        // HTTP routes
        Spark.delete("/db", this::clear);
        Spark.post("/user", userHandler::handleUserRegistration);
        Spark.post("/session", userHandler::handleUserLogin);
        Spark.delete("/session", userHandler::handleUserLogout);
        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);

        // Exception handling
        Spark.exception(BadRequestException.class, this::badRequestExceptionHandler);
        Spark.exception(UnauthorizedException.class, this::unauthorizedExceptionHandler);
        Spark.exception(Exception.class, this::genericExceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    public void clearDB() {
        userService.clear();
        gameService.clear();
        liveGames.clear(); // 🧹 clear in-memory cache too
    }

    private Object clear(Request req, Response resp) {
        clearDB();
        resp.status(200);
        return "{}";
    }

    private void badRequestExceptionHandler(BadRequestException ex, Request req, Response resp) {
        resp.status(400);
        resp.body("{ \"message\": \"Error: bad request\" }");
    }

    private void unauthorizedExceptionHandler(UnauthorizedException ex, Request req, Response resp) {
        resp.status(401);
        resp.body("{ \"message\": \"Error: unauthorized\" }");
    }

    private void genericExceptionHandler(Exception ex, Request req, Response resp) {
        resp.status(500);
        resp.body("{ \"message\": \"Error: %s\" }".formatted(ex.getMessage()));
    }
}