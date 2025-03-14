package server;

import dataaccess.*;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {

    private final UserService userService;
    private final GameService gameService;

    private final UserHandler userHandler;
    private final GameHandler gameHandler;

    public Server() {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        userHandler = new UserHandler(userService);
        gameHandler = new GameHandler(gameService);

        try { DatabaseManager.createDatabase(); } catch (DataAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // User routes
        Spark.post("/user", userHandler::handleUserRegistration);
        Spark.post("/session", userHandler::handleUserLogin);
        Spark.delete("/session", userHandler::handleUserLogout);

        // Game routes
        Spark.get("/game", gameHandler::handleListGames);
        Spark.post("/game", gameHandler::handleCreateGame);
        Spark.put("/game", gameHandler::handleJoinGame);

        // Database clear route
        Spark.delete("/db", this::clearDatabase);

        // Exception handling
        Spark.exception(BadRequestException.class, this::handleBadRequestException);
        Spark.exception(UnauthorizedException.class, this::handleUnauthorizedException);
        Spark.exception(Exception.class, this::handleGenericException);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object clearDatabase(Request req, Response resp) {
        userService.clear();
        gameService.clear();
        resp.status(200);
        return "{}";
    }

    private void handleBadRequestException(BadRequestException ex, Request req, Response resp) {
        resp.status(400);
        resp.body("{ \"message\": \"Error: bad request\" }");
    }

    private void handleUnauthorizedException(UnauthorizedException ex, Request req, Response resp) {
        resp.status(401);
        resp.body("{ \"message\": \"Error: unauthorized\" }");
    }

    private void handleGenericException(Exception ex, Request req, Response resp) {
        resp.status(500);
        resp.body("{ \"message\": \"Error: %s\" }".formatted(ex.getMessage()));
    }
}
