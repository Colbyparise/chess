package server;
import com.google.gson.Gson;
import service.UserService;
import service.GameService;
import spark.Response;
import spark.Request;

public class ClearHandler {
    private final UserService userService;
    private final GameService gameService;
    private final Gson gson = new Gson();

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
