package client;

import com.google.gson.Gson;
import model.GameData;
import model.GamesList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class ServerFacade {

    private final String baseURL = "http://localhost:8080";
    private String authToken;

    public ServerFacade() {}

    public boolean register(String username, String password, String email) {
        var payload = Map.of("username", username, "password", password, "email", email);
        var jsonPayload = new Gson().toJson(payload);
        Map response = sendRequest("POST", "/user", jsonPayload);

        if (response.containsKey("Error")) {
            return false;
        }

        authToken = (String) response.get("authToken");
        return true;
    }

    public boolean login(String username, String password) {
        var payload = Map.of("username", username, "password", password);
        var jsonPayload = new Gson().toJson(payload);
        Map response = sendRequest("POST", "/session", jsonPayload);

        if (response.containsKey("Error")) {
            return false;
        }

        authToken = (String) response.get("authToken");
        return true;
    }

    public boolean logout() {
        try {
            URI uri = new URI(baseURL + "/session");
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod("DELETE");
            if (authToken != null) {
                http.addRequestProperty("authorization", authToken);
            }
            http.connect();
            if (http.getResponseCode() != 200) {
                return false;
            }
            authToken = null;
            return true;
        } catch (URISyntaxException | IOException e) {
            return false;
        }
    }

    public int createGame(String gameName) {
        var payload = Map.of("gameName", gameName);
        var jsonPayload = new Gson().toJson(payload);
        Map response = sendRequest("POST", "/game", jsonPayload);

        double gameID = (double) response.get("gameID");
        return (int) gameID;
    }

    public HashSet<GameData> listGames() {
        String response = sendRequestString("GET", "/game");

        if (response.contains("Error")) {
            return new HashSet<>(8);
        }

        GamesList gamesList = new Gson().fromJson(response, GamesList.class);
        return gamesList.games();
    }

    public boolean joinGame(int gameId, String playerColor) {
        Map payload = (playerColor != null)
                ? Map.of("gameID", gameId, "playerColor", playerColor)
                : Map.of("gameID", gameId);

        var jsonPayload = new Gson().toJson(payload);
        Map response = sendRequest("PUT", "/game", jsonPayload);

        return !response.containsKey("Error");
    }

    private Map sendRequest(String method, String endpoint) {
        return sendRequest(method, endpoint, null);
    }

    private Map sendRequest(String method, String endpoint, String body) {
        try {
            URI uri = new URI(baseURL + endpoint);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

            connection.setRequestMethod(method);

            if (authToken != null) {
                connection.addRequestProperty("authorization", authToken);
            }

            if (body != null) {
                connection.setDoOutput(true);
                connection.addRequestProperty("Content-Type", "application/json");

                try (var outputStream = connection.getOutputStream()) {
                    outputStream.write(body.getBytes());
                }
            }

            connection.connect();

            if (connection.getResponseCode() == 401) {
                return Map.of("Error", 401);
            }

            try (InputStream inputStream = connection.getInputStream()) {
                var reader = new InputStreamReader(inputStream);
                return new Gson().fromJson(reader, Map.class);
            }

        } catch (URISyntaxException | IOException e) {
            return Map.of("Error", e.getMessage());
        }
    }

    private String sendRequestString(String method, String endpoint) {
        return sendRequestString(method, endpoint, null);
    }

    private String sendRequestString(String method, String endpoint, String body) {
        try {
            URI uri = new URI(baseURL + endpoint);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

            connection.setRequestMethod(method);

            if (authToken != null) {
                connection.addRequestProperty("authorization", authToken);
            }

            if (body != null) {
                connection.setDoOutput(true);
                connection.addRequestProperty("Content-Type", "application/json");

                try (var outputStream = connection.getOutputStream()) {
                    outputStream.write(body.getBytes());
                }
            }

            connection.connect();

            if (connection.getResponseCode() == 401) {
                return "Error: 401";
            }

            try (InputStream inputStream = connection.getInputStream()) {
                var reader = new InputStreamReader(inputStream);
                return readStream(reader);
            }

        } catch (URISyntaxException | IOException e) {
            return String.format("Error: %s", e.getMessage());
        }
    }

    private String readStream(InputStreamReader reader) {
        StringBuilder sb = new StringBuilder();

        try {
            for (int ch; (ch = reader.read()) != -1; ) {
                sb.append((char) ch);
            }
        } catch (IOException e) {
            return "";
        }

        return sb.toString();
    }
}

