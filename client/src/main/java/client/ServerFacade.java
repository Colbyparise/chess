package client;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.GamesList;
import model.UserData;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


public class ServerFacade {
    private final String baseURL;


    public ServerFacade(int port) {
        this.baseURL = "http://localhost:" + port;
    }

    public int createGame(String gameName, String authToken) {
        var body = Map.of("gameName", gameName);
        var jsonBody = new Gson().toJson(body);
        Map resp = request("POST", "/game", jsonBody, authToken);
        if (resp.containsKey("Error")) {
            return -1;
        }
        double gameID = (double) resp.get("gameID");
        return (int) gameID;
    }

    public HashSet<GameData> listGames(String authToken) {
        String resp = requestString("GET", "/game", authToken);
        if (resp.contains("Error")) {
            return new HashSet<>();
        }
        GamesList games = new Gson().fromJson(resp, GamesList.class);
        return games.games();
    }

    public AuthData login(String username, String password) throws Exception {
        var credentials = Map.of(
                "username", username,
                "password", password
        );
        var jsonBody = new Gson().toJson(credentials);
        Map<String, Object> response = request("POST", "/session", jsonBody, null);
        if (response.containsKey("Error")) {
            throw new Exception((String) response.get("Error"));
        }
        return new Gson().fromJson(new Gson().toJson(response), AuthData.class);
    }

    public AuthData register(UserData user) throws Exception {
        var jsonBody = new Gson().toJson(user);
        Map<String, Object> response = request("POST", "/user", jsonBody, null);
        if (response.containsKey("Error")) {
            throw new Exception((String) response.get("Error"));
        }
        return new Gson().fromJson(new Gson().toJson(response), AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        Map<String, Object> response = request("DELETE", "/session", null, authToken);
        if (response.containsKey("Error")) {
            throw new Exception((String) response.get("Error"));
        }
    }

    public boolean joinGame(int gameId, ChessGame.TeamColor color, String authToken) throws Exception {
        Map<String, Object> body;
        if (color != null) {
            body = Map.of("gameID", gameId, "playerColor", color.name());
        } else {
            body = Map.of("gameID", gameId);
        }
        String jsonBody = new Gson().toJson(body);
        Map<String, Object> response = request("PUT", "/game", jsonBody, authToken);
        return !response.containsKey("Error");
    }

    public void observeGame(int gameID, String authToken) throws Exception {
        Map<String, Object> body = Map.of("gameID", gameID);
        String jsonBody = new Gson().toJson(body);
        Map<String, Object> response = request("PUT", "/game", jsonBody, authToken);
        if (response.containsKey("Error")) {
            throw new Exception((String) response.get("Error"));
        }
    }
    public void clearDB() throws Exception {
        Map<String, Object> response = request("DELETE", "/db", null, null);
        if (response.containsKey("Error")) {
            throw new Exception((String) response.get("Error"));
        }
    }


    private Map<String, Object> request(String method, String path, String bodyJson, String authToken) {
        try {
            URL url = new URL(baseURL + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            if (authToken != null) {
                connection.setRequestProperty("Authorization", authToken);
            }
            if (method.equals("POST") || method.equals("PUT")) {
                connection.setDoOutput(true);
                if (bodyJson != null) {
                    try (OutputStream os = connection.getOutputStream()) {
                        os.write(bodyJson.getBytes());
                    }
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

    private String requestString(String method, String path, String authToken) {
        try {
            URL url = new URL(baseURL + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Accept", "application/json");
            if (authToken != null) {
                connection.setRequestProperty("Authorization", authToken);
            }
            try (var inputStream = connection.getInputStream()) {
                return new String(inputStream.readAllBytes());
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
