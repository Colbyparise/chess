package server;

import com.google.gson.Gson;
import dataAccess.BadRequestException;
import dataAccess.UnauthorizedException;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.HashSet;

public class GameHandler {

    GameService gameService;
    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object listGames(Request req, Response resp) throws UnauthorizedException {
        String authToken = req.headers("authorization");
        HashSet<GameData> games = gameService.listGames(authToken);
        resp.status(200);
        return new Gson().toJson(new ListGamesResponse(games));
    }

    private record ListGamesResponse(HashSet<GameData> games) {
    }

    public Object createGame(Request req, Response resp) throws BadRequestException, UnauthorizedException {
        record CreateGameRequest(String gameName) {}
        CreateGameRequest createGameRequest = new Gson().fromJson(req.body(), CreateGameRequest.class);

        if (createGameRequest == null || createGameRequest.gameName() == null || createGameRequest.gameName().isBlank()) {
            throw new BadRequestException("No gameName provided");
        }

        String authToken = req.headers("authorization");
        int gameID = gameService.createGame(authToken, createGameRequest.gameName());

        resp.status(200);
        return "{ \"gameID\": %d }".formatted(gameID);
    }

    public Object joinGame(Request req, Response resp) throws BadRequestException, UnauthorizedException {

        if (!req.body().contains("\"gameID\":")) {
            throw new BadRequestException("No gameID provided");
        }

        String authToken = req.headers("authorization");
        record JoinGameData(String playerColor, int gameID) {}
        JoinGameData joinData = new Gson().fromJson(req.body(), JoinGameData.class);
        boolean joinSuccess =  gameService.joinGame(authToken, joinData.gameID(), joinData.playerColor());

        if (!joinSuccess) {
            resp.status(403);
            return "{ \"message\": \"Error: already taken\" }";
        }

        resp.status(200);
        return "{}";
    }
}
