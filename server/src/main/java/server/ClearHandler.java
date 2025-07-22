package server;
import com.google.gson.Gson;
import dataaccess.ErrorResponse;
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
        try {
            userService.clear(); // Clears users and authTokens
            gameService.clear(); // Clears games
            res.status(200);
            return "{}";
        } catch (Exception e) {
            res.status(500);
            return new Gson().toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
}
