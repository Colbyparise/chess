package client;

import com.google.gson.Gson;
import model.GameData;
import model.GamesList;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class ServerFacade {

    private String apiBase = "http://localhost:8080";
    private String token;

    public ServerFacade() {}

    public ServerFacade(String url) {
        this.apiBase = url;
    }

    public boolean register(String username, String password, String email) {
        Map<String, String> payload = Map.of(
                "username", username,
                "password", password,
                "email", email
        );

        String requestBody = new Gson().toJson(payload);
        Map<String, Object> response = sendRequest("POST", "/user", requestBody);

        if (response.containsKey("Error")) return false;

        token = (String) response.get("authToken");
        return true;
    }

    public boolean login(String username, String password) {
        Map<String, String> payload = Map.of(
                "username", username,
                "password", password
        );

        String requestBody = new Gson().toJson(payload);
        Map<String, Object> response = sendRequest("POST", "/session", requestBody);

        if (response.containsKey("Error")) return false;

        token = (String) response.get("authToken");
        return true;
    }

    public boolean logout() {
        Map<String, Object> response = sendRequest("DELETE", "/session");
        if (response.containsKey("Error")) return false;

        token = null;
        return true;
    }

    public int createGame(String name) {
        String body = new Gson().toJson(Map.of("gameName", name));
        Map<String, Object> response = sendRequest("POST", "/game", body);

        if (response.containsKey("Error")) return -1;

        return ((Double) response.get("gameID")).intValue();
    }

    public HashSet<GameData> listGames() {
        String result = sendRawRequest("GET", "/game");

        if (result.contains("Error")) {
            return new HashSet<>();
        }

        GamesList parsedList = new Gson().fromJson(result, GamesList.class);
        return parsedList.games();
    }

    public boolean joinGame(int gameId, String color) {
        Map<String, Object> payload = (color == null)
                ? Map.of("gameID", gameId)
                : Map.of("gameID", gameId, "playerColor", color);

        String body = new Gson().toJson(payload);
        Map<String, Object> response = sendRequest("PUT", "/game", body);

        return !response.containsKey("Error");
    }

    // --- Internal helpers below ---

    private Map<String, Object> sendRequest(String method, String path) {
        return sendRequest(method, path, null);
    }

    private Map<String, Object> sendRequest(String method, String path, String requestBody) {
        Map<String, Object> parsedResponse;

        try {
            URI target = new URI(apiBase + path);
            HttpURLConnection connection = (HttpURLConnection) target.toURL().openConnection();
            connection.setRequestMethod(method);

            if (token != null) {
                connection.setRequestProperty("authorization", token);
            }

            if (requestBody != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(requestBody.getBytes());
                }
            }

            connection.connect();

            if (connection.getResponseCode() == 401) {
                return Map.of("Error", 401);
            }

            try (InputStream input = connection.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(input);
                parsedResponse = new Gson().fromJson(reader, Map.class);
            }

        } catch (URISyntaxException | IOException e) {
            return Map.of("Error", e.getMessage());
        }

        return parsedResponse;
    }

    private String sendRawRequest(String method, String path) {
        return sendRawRequest(method, path, null);
    }

    private String sendRawRequest(String method, String path, String requestBody) {
        try {
            URI target = new URI(apiBase + path);
            HttpURLConnection connection = (HttpURLConnection) target.toURL().openConnection();
            connection.setRequestMethod(method);

            if (token != null) {
                connection.setRequestProperty("authorization", token);
            }

            if (requestBody != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(requestBody.getBytes());
                }
            }

            connection.connect();

            if (connection.getResponseCode() == 401) {
                return "Error: 401";
            }

            try (InputStream input = connection.getInputStream()) {
                return readStreamToString(new InputStreamReader(input));
            }

        } catch (URISyntaxException | IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String readStreamToString(InputStreamReader reader) {
        StringBuilder output = new StringBuilder();

        try {
            int ch;
            while ((ch = reader.read()) != -1) {
                output.append((char) ch);
            }
        } catch (IOException e) {
            return "";
        }

        return output.toString();
    }
}
