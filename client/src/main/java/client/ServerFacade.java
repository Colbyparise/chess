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

    HttpCommunicator http;
    WebsocketCommunicator ws;
    String serverDomain;
    String authToken;

    public ServerFacade() throws Exception {
        this("localhost:8080");
    }

    public ServerFacade(String serverDomain) throws Exception {
        this.serverDomain = serverDomain;
        http = new HttpCommunicator(this, serverDomain);
        ws = new WebsocketCommunicator(serverDomain);
    }

    protected String getAuthToken() {
        return authToken;
    }

    protected void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean register(String username, String password, String email) {
        return http.register(username, password, email);
    }

    public boolean login(String username, String password) {
        return http.login(username, password);
    }

    public boolean logout() {
        return http.logout();
    }

    public int createGame(String gameName) {
        return http.createGame(gameName);
    }

    public HashSet<GameData> listGames() {
        return http.listGames();
    }

    public boolean joinGame(int gameId, String playerColor) {
        return http.joinGame(gameId, playerColor);
    }

    public void sendWSMessage(String message) {
        ws.sendMessage(message);
    }

}