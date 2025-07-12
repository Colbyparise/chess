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

    public Object ListGamesHandler(Request req, Response resp, Gson gson) throws DataAccessException {
        String authToken = req.headers("authorization");
        Set<GameData> games = gameService.listGames(authToken);

        resp.status(200);
        return gson.toJson(new ListGamesResponse(games));

    }


    public Object createGameHandler(Request req, Response resp) throws DataAccessException {
        CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);

        if (request == null || request.gameName() == null || request.gameName().isBlank()) {
            throw new DataAccessException("Game name is required.");
        }

        String authToken = req.headers("authorization");
        int gameID = gameService.createGame(authToken, request.gameName());

        resp.status(200);
        return gson.toJson(new CreateGameResponse(gameID));
    }

    public Object joinGameHandler(Request req, Response resp) throws DataAccessException {
        String authToken = req.headers("authorization");

        JoinGameRequest request = gson.fromJson(req.body(), JoinGameRequest.class);

        if (request == null || request.gameID() <= 0) {
            throw new DataAccessException("Valid gameID is required.");
        }

        boolean success = gameService.joinGame(authToken, request.gameID(), request.playerColor());

        if (!success) {
            resp.status(403);
            return gson.toJson(new ErrorResponse("Error: Spot already taken."));
        }

        resp.status(200);
        return "{}";
    }

    private record CreateGameRequest(String gameName) {}
    private record ListGamesResponse(Set<GameData> games) {}
    private record CreateGameResponse(int gameID) {}
    private record JoinGameRequest(String playerColor, int gameID) {}
    private record ErrorResponse(String message) {}
}
