package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void add(String username, int gameId, Session session) {
        Connection connection = new Connection(username, gameId, session);
        connections.put(username, connection);
    }

    public void remove(String username) {
        connections.remove(username);
    }

    public List<String> getGameConnections(int gameId) {
        List<String> userList = new ArrayList<>();
        for (Connection conn : connections.values()) {
            if (conn.gameId == gameId) {
                userList.add(conn.username);
            }
        }
        return userList;
    }

    public void broadcast(String excludeUsername, int gameId, ServerMessage message) throws IOException {
        List<Connection> toRemove = new ArrayList<>();

        for (Connection conn : connections.values()) {
            if (conn.session.isOpen()) {
                if (conn.gameId == gameId && !conn.username.equals(excludeUsername)) {
                    conn.send(gson.toJson(message));
                }
            } else {
                toRemove.add(conn);
            }
        }

        for (Connection conn : toRemove) {
            connections.remove(conn.username);
        }
    }
}
