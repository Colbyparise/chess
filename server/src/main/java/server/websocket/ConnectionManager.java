package server.websocket;
import org.eclipse.jetty.websocket.api.Session;

import java.util.HashMap;
import java.util.Map;

public class ConnectionManager {
    private final Map<Session, String> sessionTokens = new HashMap<>();

    public void addSession(Session session, String authToken) {
        sessionTokens.put(session, authToken);
    }

    public String getAuthToken(Session session) {
        return sessionTokens.get(session);
    }
}