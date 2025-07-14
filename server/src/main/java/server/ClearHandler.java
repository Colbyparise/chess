package server;
import service.UserService;
import service.GameService;
import spark.Response;
import spark.Request;

public class ClearHandler {
    private final UserService userService;
    private final GameService gameService;

    public ClearHandler(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    public Object handleClear(Request req, Response res) {
        userService.clear();
        gameService.clear();
        res.status(200);
        return "{}";
    }
}

