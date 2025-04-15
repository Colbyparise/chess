package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    private Gson gson;

    public ConnectionManager() {
        gson = new Gson();
    }

    public void add(String username, int gameId, Session session) {
        var connection = new Connection(username, gameId, session);
        connections.put(username, connection);
    }

    public void remove(String visitorName) {
        connections.remove(visitorName);
    }

    public List<String> getGameConnections(int id) {
        var returnVal = new ArrayList<String>();
        for (var c : connections.values()) {
            if (c.gameId == id) {
                returnVal.add(c.username);
            }
        }
        return returnVal;
    }

    public void broadcast(String excludeUsername, int gameId, ServerMessage notification) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (c.gameId == gameId && !c.username.equals(excludeUsername)) {
                    c.send(gson.toJson(notification));
                }
            }
            else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.username);
        }
    }
}