package server;

import com.google.gson.Gson;
import dataAccess.BadRequestException;
import dataAccess.UnauthorizedException;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.Set;

public class GameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object handleListGames(Request req, Response resp) throws UnauthorizedException {
        String authToken = req.headers("authorization");
        Set<GameData> games = gameService.listGames(authToken);

        resp.status(200);
        return gson.toJson(new ListGamesResponse(games));
    }

    public Object handleCreateGame(Request req, Response resp) throws BadRequestException, UnauthorizedException {
        CreateGameRequest request = gson.fromJson(req.body(), CreateGameRequest.class);

        if (request == null || request.gameName() == null || request.gameName().isBlank()) {
            throw new BadRequestException("Game name is required.");
        }

        String authToken = req.headers("authorization");
        int gameID = gameService.createGame(authToken, request.gameName());

        resp.status(200);
        return gson.toJson(new CreateGameResponse(gameID));
    }

    public Object handleJoinGame(Request req, Response resp) throws BadRequestException, UnauthorizedException {
        String authToken = req.headers("authorization");

        JoinGameRequest request = gson.fromJson(req.body(), JoinGameRequest.class);

        if (request == null || request.gameID() <= 0) {
            throw new BadRequestException("Valid gameID is required.");
        }

        boolean success = gameService.joinGame(authToken, request.gameID(), request.playerColor());

        if (!success) {
            resp.status(403);
            return gson.toJson(new ErrorResponse("Error: Spot already taken."));
        }

        resp.status(200);
        return "{}";
    }

    // Response & Request DTOs
    private record ListGamesResponse(Set<GameData> games) {}
    private record CreateGameRequest(String gameName) {}
    private record CreateGameResponse(int gameID) {}
    private record JoinGameRequest(String playerColor, int gameID) {}
    private record ErrorResponse(String message) {}
}
