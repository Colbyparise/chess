package server;

import com.google.gson.Gson;
import dataAccess.BadRequestException;
import dataAccess.UnauthorizedException;
import model.GameData;
import model.GamesList;
import service.GameService;
import spark.Request;
import spark.Response;

public class GameHandler {
    GameService gameService;
    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object listGames(Request request, Response response) throws UnauthorizedException {
        String authToken = request.headers("authorization");
        GamesList games = new GamesList(gameService.listGames(authToken));
        response.status(200);
        return new Gson().toJson(games);
    }

    public Object createGame(Request request, Response response) throws BadRequestException, UnauthorizedException {

        if (!request.body().contains("\"gameName provided")) {
            throw new BadRequestException("No game name provided");
        }

        GameData gameData = new Gson().fromJson(request.body(), GameData.class);

        String authToken = request.headers("authorization");
        int gameID = gameService.createGame(authToken, gameData.gameName());

        response.status(200);
        return "{ \"gameID\": %d }".formatted(gameID);
    }

    public Object joinGame(Request request, Response response) throws BadRequestException, UnauthorizedException {

        if (!request.body().contains("\"gameID\":")) {
            throw new BadRequestException("No game ID provided");
        }
        String authToken = request.headers("authorization");
        record JoinGameData(String playerColor, int gameID) {
        }
        JoinGameData joinData = new Gson().fromJson(request.body(), JoinGameData.class);
        boolean joinSuccess = gameService.joinGame(authToken, joinData.gameID(), joinData.playerColor());

        if (!joinSuccess) {
            response.status(403);
            return "{\"message\": \"Error: already taken\" }";
        }

        response.status(200);
        return "{}";
    }
}
