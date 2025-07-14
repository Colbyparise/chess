package server;
import com.google.gson.Gson;

import dataaccess.DataAccessException;
import spark.Request;
import model.GameData;
import service.GameService;
import spark.Response;
import java.util.Set;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object listGamesHandler(Request req, Response resp) throws DataAccessException {
        String authToken = req.headers("authorization");
        Set<GameData> games = gameService.listGames(authToken);

        resp.status(200);
        return gson.toJson(new ListGamesResponse(games));

    }


    public Object createGameHandler(Request req, Response resp) {
        try {
            CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);

            if (request == null || request.gameName() == null || request.gameName().isBlank()) {
                resp.status(400);
                return gson.toJson(new ErrorResponse("Error: Game name is required."));
            }

            String authToken = req.headers("authorization");

            if (authToken == null || authToken.isBlank()) {
                resp.status(401);
                return gson.toJson(new ErrorResponse("Error: Missing authorization token."));
            }

            int gameID = gameService.createGame(authToken, request.gameName());
            resp.status(200);
            return gson.toJson(new CreateGameResponse(gameID));
        } catch (DataAccessException exception) {
            resp.status(401);
            return gson.toJson(new ErrorResponse("Error: " + exception.getMessage()));
        }
    }

    public Object joinGameHandler(Request req, Response resp) throws DataAccessException {
        try {
            String authToken = req.headers("authorization");

            JoinGameRequest request = gson.fromJson(req.body(), JoinGameRequest.class);

            if (request == null || request.gameID() <= 0) {
                resp.status(400);
                return gson.toJson(new ErrorResponse("Error: Valid gameID is required."));
            }

            if (request.playerColor() == null || request.playerColor().isBlank()) {
                resp.status(400);
                return gson.toJson(new ErrorResponse("Error: Team color cannot be null or empty"));
            }

            boolean success = gameService.joinGame(authToken, request.gameID(), request.playerColor());

            if (!success) {
                resp.status(403);
                return gson.toJson(new ErrorResponse("Error: Spot already taken."));
            }

            resp.status(200);
            return "{}";

        } catch (DataAccessException exception) {
            if (exception.getMessage().contains("not a valid team color")) {
                resp.status(400);
            } else if (exception.getMessage().contains("Auth Token does not exist")) {
                resp.status(401);
            } else {
                resp.status(500);
            }

            return gson.toJson(new ErrorResponse("Error: " + exception.getMessage()));
        }
    }


    private record CreateGameRequest(String gameName) {}
    private record ListGamesResponse(Set<GameData> games) {}
    private record CreateGameResponse(int gameID) {}
    private record JoinGameRequest(String playerColor, int gameID) {}
    private record ErrorResponse(String message) {}
}
