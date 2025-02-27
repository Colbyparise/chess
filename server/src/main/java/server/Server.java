package server;

import dataAccess.*;
import org.eclipse.jetty.websocket.api.Session;
import service.GameService;
import service.UserService;
import static spark.*;

import java.util.concurrent.ConcurrentHashMap;

public class Server {

    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    static UserService userHandler;
    static GameService gameHandler;
    static ConcurrentHashMap<Session, Integer> gameSessions = new ConcurrentHashMap<>();

    public Server() {
        userDAO = new SQLUserDAO();
        authDAO = new SQLAuthDAO();
        gameDAO = new SQLGameDAO();

        userService = new UserService(userDAO, authDAO);
        gameService = new UserHandler(userService);
        userHandler = new UserHandler(userService);
        gameHandler = new GameHandler() (gameService);

        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    }
    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.webSocket("/connect", WebsocketHandler.class);
        Spark.delete("/db", this::clear);
        Spark.post("/user", userHandler::register);
        Spark.post("/session", userHandler::login);
        Spark.delete("/session", userHandler::logout);

        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);

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
    }

    private Object clear(Request request, Response response) {
        clearDB();

        response.status(200);
        return "{}";
    }

    private void badRequestExceptionHandler(BadRequestException exception, Request request, Response response) {
        response.status(400);
        response.body("{ \"message\": \"Error: bad request\" }");
    }

    private void unauthorizedExceptionHandler(UnauthorizedException ex, Request req, Response resp) {
        resp.status(401);
        resp.body("{ \"message\": \"Error: unauthorized\" }");
    }

    private void genericExceptionHandler(Exception ex, Request req, Response resp) {
        resp.status(500);
        resp.body("{ \"message\": \"Error: %s\" }".formatted(ex.getMessage()));
    }


