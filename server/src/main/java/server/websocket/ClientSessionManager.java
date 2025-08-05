package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSessionManager {
    private static final Map<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();
    private static final Map<Session, String> sessionToUsername = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, ClientInfo> sessionInfo = new ConcurrentHashMap<>();

    public static void addToGame(int gameID, Session session, String username) {
        gameSessions.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionToUsername.put(session, username);
    }

    public static void removeFromGame(int gameID, Session session) {
        Set<Session> sessions = gameSessions.getOrDefault(gameID, Set.of());
        sessions.remove(session);
        sessionToUsername.remove(session);
        sessionInfo.remove(session);
    }

    public static void registerSession(Session session, int gameId, String username) {
        sessionInfo.put(session, new ClientInfo(gameId, username));
    }

    public static void removeSession(Session session) {
        sessionInfo.remove(session);
    }

    public static void broadcastToGame(int gameID, ServerMessage message) {
        broadcastToGame(gameID, message, null);
    }

    public static void broadcastToGame(int gameID, ServerMessage message, Session except) {
        for (Session s : gameSessions.getOrDefault(gameID, Set.of())) {
            if (!s.equals(except)) {
                WebSocketHandler.sendToSession(s, message);
            }
        }
    }

    public static void broadcastToGameExcept(int gameId, String usernameToExclude, ServerMessage message) {
        for (var entry : sessionInfo.entrySet()) {
            ClientInfo info = entry.getValue();
            if (info.gameID() == gameId && !info.username().equals(usernameToExclude)) {
                WebSocketHandler.sendToSession(entry.getKey(), message);
            }
        }
    }


    public static String getUsername(Session session) {
        return sessionToUsername.get(session);
    }
}
