package server;

import dataaccess.*;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // DAOs
        UserDAO userDAO = new SQLUserDAO();
        AuthDAO authDAO = new SQLAuthDAO();
        GameDAO gameDAO = new SQLGameDAO();

        // services
        UserService userService = new UserService(userDAO ,authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        //Handlers
        UserHandler userHandler = new UserHandler(userService);
        GameHandler gameHandler = new GameHandler(gameService);
        ClearHandler clearHandler = new ClearHandler(userService, gameService);

        //endpoints
        Spark.delete("/db", clearHandler::handleClear);

        Spark.post("/user", userHandler::registrationHandler);
        Spark.post("/session", userHandler::loginHandler);
        Spark.delete("/session", userHandler::logoutHandler);

        Spark.get("/game", gameHandler::listGamesHandler);
        Spark.post("/game", gameHandler::createGameHandler);
        Spark.put("/game", gameHandler::joinGameHandler);

        // Global Exception Handling (optional but helpful)
//        Spark.exception(Exception.class, (e, req, res) -> {
//            res.status(401);
//            res.body("{\"message\":\"Error: " + e.getMessage() + "\"}");
//        });

        Spark.exception(Exception.class, (e, req, res) -> {
            if (e instanceof UnauthorizedException) {
                res.status(401);
            } else if (e instanceof DataAccessException) {
                res.status(500);
            } else {
                res.status(500);
            }
            res.body("{\"message\":\"Error: " + e.getMessage() + "\"}");
        });


        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
