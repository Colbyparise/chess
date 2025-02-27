package server;

import dataAccess.*;
import org.eclipse.jetty.websocket.api.Session;
import service.GameService;
import service.UserService;
import spark.*;

import java.util.concurrent.ConcurrentHashMap;

public class Server {

    static UserService userService;
    static GameService gameService;

    UserHandler userHandler;
    GameHandler gameHandler;

    static ConcurrentHashMap<Session, Integer> gameSessions = new ConcurrentHashMap<>();

    public Server(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;

        userHandler = new UserHandler(userService);
        gameHandler = new GameHandler(gameService);
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

    private void badRequestExceptionHandler(BadRequestException exception, Request request, Response response) {
        response.status(400);
        response.body("{ \"message\": \"Error: bad request\" }");
    }

    private void unauthorizedExceptionHandler(UnauthorizedException exception, Request request, Response response) {
        response.status(401);
        response.body("{ \"message\": \"Error: unauthorized\" }");
    }

    private void genericExceptionHandler(Exception exception, Request request, Response response) {
        response.status(500);
        response.body("{ \"message\": \"Error: %s\" }".formatted(exception.getMessage()));
    }

}