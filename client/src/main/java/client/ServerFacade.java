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

    private String baseURL = "http://localhost:8080";
    private String authToken;

    public ServerFacade() {}

    public ServerFacade(String url) {
        baseURL = url;
    }

    public boolean register(String username, String password, String email) {
        var body = Map.of("username", username, "password", password, "email", email);
        var jsonBody = new Gson().toJson(body);
        Map resp = request("POST", "/user", jsonBody);
        if (resp.containsKey("Error")) {
            return false;
        }
        authToken = (String) resp.get("authToken");
        return true;
    }

    public boolean login(String username, String password) {
        var body = Map.of("username", username, "password", password);
        var jsonBody = new Gson().toJson(body);
        Map resp = request("POST", "/session", jsonBody);
        if (resp.containsKey("Error")) {
            return false;
        }
        authToken = (String) resp.get("authToken");
        return true;
    }

    public boolean logout() {
        Map resp = request("DELETE", "/session");
        if (resp.containsKey("Error")) {
            return false;
        }
        authToken = null;
        return true;
    }

    public int createGame(String gameName) {
        var body = Map.of("gameName", gameName);
        var jsonBody = new Gson().toJson(body);
        Map resp = request("POST", "/game", jsonBody);
        if (resp.containsKey("Error")) {
            return -1;
        }
        double gameID = (double) resp.get("gameID");
        return (int) gameID;
    }

    public HashSet<GameData> listGames() {
        String resp = requestString("GET", "/game");
        if (resp.startsWith("Error")) {
            return new HashSet<>();
        }
        GamesList games = new Gson().fromJson(resp, GamesList.class);
        return games.games();
    }

    public boolean joinGame(int gameId, String playerColor) {
        Map body = playerColor != null
                ? Map.of("gameID", gameId, "playerColor", playerColor)
                : Map.of("gameID", gameId);

        var jsonBody = new Gson().toJson(body);
        Map resp = request("PUT", "/game", jsonBody);
        return !resp.containsKey("Error");
    }

    private InputStream requestRaw(String method, String endpoint, String body) throws IOException, URISyntaxException {
        URI uri = new URI(baseURL + endpoint);
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod(method);

        if (authToken != null) {
            http.addRequestProperty("Authorization", authToken);
        }

        if (body != null) {
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");
            try (var outputStream = http.getOutputStream()) {
                outputStream.write(body.getBytes());
            }
        }

        http.connect();

        if (http.getResponseCode() == 401) {
            throw new IOException("Unauthorized (401)");
        }

        return http.getInputStream();
    }

    private Map request(String method, String endpoint) {
        return request(method, endpoint, null);
    }

    private Map request(String method, String endpoint, String body) {
        try (InputStream respBody = requestRaw(method, endpoint, body)) {
            InputStreamReader inputStreamReader = new InputStreamReader(respBody);
            return new Gson().fromJson(inputStreamReader, Map.class);
        } catch (IOException | URISyntaxException e) {
            return Map.of("Error", e.getMessage());
        }
    }

    private String requestString(String method, String endpoint) {
        return requestString(method, endpoint, null);
    }

    private String requestString(String method, String endpoint, String body) {
        try (InputStream respBody = requestRaw(method, endpoint, body)) {
            InputStreamReader inputStreamReader = new InputStreamReader(respBody);
            return readerToString(inputStreamReader);
        } catch (IOException | URISyntaxException e) {
            return String.format("Error: %s", e.getMessage());
        }
    }

    private String readerToString(InputStreamReader reader) {
        StringBuilder sb = new StringBuilder();
        try {
            for (int ch; (ch = reader.read()) != -1; ) {
                sb.append((char) ch);
            }
            return sb.toString();
        } catch (IOException e) {
            return "";
        }
    }
}
