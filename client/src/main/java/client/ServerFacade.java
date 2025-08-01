package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.GamesList;
import model.UserData;

import java.io.InputStream;
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
        Map<String, Object> resp = request("POST", "/game", jsonBody, authToken);
        if (resp.containsKey("Error") || resp.containsKey("message")) {
            String message = (String) resp.getOrDefault("Error", resp.get("message"));
            throw new RuntimeException(message);
        }
        double gameID = (double) resp.get("gameID");
        return (int) gameID;
    }

    public HashSet<GameData> listGames(String authToken) {
        String resp = requestString("GET", "/game", authToken);
        if (resp.startsWith("Error")) {
            throw new RuntimeException(resp);
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
        if (response.containsKey("Error") || response.containsKey("message")) {
            String message = (String) response.getOrDefault("Error", response.get("message"));
            throw new Exception(message);
        }
        return new Gson().fromJson(new Gson().toJson(response), AuthData.class);
    }

    public AuthData register(UserData user) throws Exception {
        var jsonBody = new Gson().toJson(user);
        Map<String, Object> response = request("POST", "/user", jsonBody, null);
        if (response.containsKey("Error") || response.containsKey("message")) {
            String message = (String) response.getOrDefault("Error", response.get("message"));
            throw new Exception(message);
        }
        return new Gson().fromJson(new Gson().toJson(response), AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        Map<String, Object> response = request("DELETE", "/session", null, authToken);
        if (response.containsKey("Error") || response.containsKey("message")) {
            String message = (String) response.getOrDefault("Error", response.get("message"));
            throw new Exception(message);
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
        if (response.containsKey("Error") || response.containsKey("message")) {
            String message = (String) response.getOrDefault("Error", response.get("message"));
            throw new Exception(message);
        }

        return true;
    }

    public void observeGame(int gameID, String authToken) throws Exception {
        Map<String, Object> body = Map.of("gameID", gameID);
        String jsonBody = new Gson().toJson(body);
        Map<String, Object> response = request("PUT", "/game/observe", jsonBody, authToken);
        if (response.containsKey("Error") || response.containsKey("message")) {
            String message = (String) response.getOrDefault("Error", response.get("message"));
            throw new Exception(message);
        }
    }
    public void clearDB() throws Exception {
        Map<String, Object> response = request("DELETE", "/db", null, null);
        if (response.containsKey("Error") || response.containsKey("message")) {
            String message = (String) response.getOrDefault("Error", response.get("message"));
            throw new Exception(message);
        }
    }

    public ChessBoard getGameBoard(int gameID, String authToken) throws Exception {
        String path = "/game/" + gameID;
        String response = requestString("GET", path, authToken);

        if (response.startsWith("Error")) {
            throw new Exception(response);
        }

        return new Gson().fromJson(response, ChessBoard.class);
    }
    public void leaveGame(int gameId, String authToken) throws Exception {
        var body = Map.of("gameID", gameId);
        var jsonBody = new Gson().toJson(body);
        Map<String, Object> response = request("POST", "/game/leave", jsonBody, authToken);

        if (response.containsKey("Error") || response.containsKey("message")) {
            String message = (String) response.getOrDefault("Error", response.get("message"));
            throw new Exception(message);
        }
    }

    public void makeMove(int gameId, String authToken, ChessMove move) throws Exception {
        var body = Map.of(
                "gameID", gameId,
                "move", move
        );
        var jsonBody = new Gson().toJson(body);
        Map<String, Object> response = request("POST", "/game/move", jsonBody, authToken);

        if (response.containsKey("Error") || response.containsKey("message")) {
            String message = (String) response.getOrDefault("Error", response.get("message"));
            throw new Exception(message);
        }
    }

    public void resignGame(int gameId, String authToken) throws Exception {
        var body = Map.of("gameID", gameId);
        var jsonBody = new Gson().toJson(body);
        Map<String, Object> response = request("POST", "/game/resign", jsonBody, authToken);

        if (response.containsKey("Error") || response.containsKey("message")) {
            String message = (String) response.getOrDefault("Error", response.get("message"));
            throw new Exception(message);
        }
    }


    private Map<String, Object> request(String method, String path, String bodyJson, String authToken) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseURL + path);
            connection = (HttpURLConnection) url.openConnection();
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

            int responseCode = connection.getResponseCode();
            InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            String response = new String(inputStream.readAllBytes());
            return new Gson().fromJson(response, Map.class);

        } catch (Exception e) {
            try {
                if (connection != null) {
                    InputStream errorStream = connection.getErrorStream();
                    if (errorStream != null) {
                        String response = new String(errorStream.readAllBytes());
                        return new Gson().fromJson(response, Map.class);
                    }
                }
            } catch (Exception inner) {
                // Ignore, fall through
            }
            return Map.of("Error", "Network error: " + e.getMessage());
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
