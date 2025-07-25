package client;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import model.GamesList;

import java.util.HashSet;
import java.util.Map;

public class ServerFacade {
    String baseURL = "http://localhost:8080";

    public ServerFacade() {
    }

    public ServerFacade(String url) {
        baseURL = url;
    }

    public int createGame(String gameName, String authToken) {
        var body = Map.of("gameName", gameName);
        var jsonBody = new Gson().toJson(body);
        Map resp = request("POST", "/game", jsonBody);
        if (resp.containsKey("Error")) {
            return -1;
        }
        double gameID = (double) resp.get("gameID");
        return (int) gameID;
    }

        public HashSet<GameData> listGames(String authToken) {
        String resp = requestString("GET", "/game");
        if (resp.contains("Error")) {
            return HashSet.newHashSet(8);
        }
        GamesList games = new Gson().fromJson(resp, GamesList.class);

        return games.games();
    }

    public boolean joinGame(int gameId, ChessGame.TeamColor color, String authToken) {
        Map body;
        if (color != null) {
            body = Map.of("gameID", gameId, "playerColor", color);
        } else {
            body = Map.of("gameID", gameId);
        }
        var jsonBody = new Gson().toJson(body);
        Map resp = request("PUT", "/game", jsonBody);
        return !resp.containsKey("Error");
    }

    public void observeGame(int gameID, String authToken) {

    }
    private Map<String, Object> request(String method, String path, String bodyJson) {
        try {
            java.net.URL url = new java.net.URL(baseURL + path);
            var connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            if (bodyJson != null) {
                try (var outputStream = connection.getOutputStream()) {
                    outputStream.write(bodyJson.getBytes());
                }
            }

            try (var inputStream = connection.getInputStream()) {
                String response = new String(inputStream.readAllBytes());
                return new Gson().fromJson(response, Map.class);
            }

        } catch (Exception e) {
            return Map.of("Error", e.getMessage());
        }
    }
    private String requestString(String method, String path) {
        try {
            java.net.URL url = new java.net.URL(baseURL + path);
            var connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Accept", "application/json");

            try (var inputStream = connection.getInputStream()) {
                return new String(inputStream.readAllBytes());
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}