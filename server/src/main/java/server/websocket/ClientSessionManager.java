package server.websocket;

import chess.ChessGame;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ClientSessionManager {
    private static final Map<Integer, Set<Session>> GameSessions = new ConcurrentHashMap<>();
    private static final Map<Session, String> SessionToUsername = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, ClientInfo> SessionInfo = new ConcurrentHashMap<>();

    public static void addToGame(int gameID, Session session, String username) {
        GameSessions.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(session);
        SessionToUsername.put(session, username);
    }

    public static void removeFromGame(int gameID, Session session) {
        Set<Session> sessions = GameSessions.getOrDefault(gameID, Set.of());
        sessions.remove(session);
        SessionToUsername.remove(session);
        SessionInfo.remove(session);
    }

    public static void registerSession(Session session, int gameId, String username) {
        SessionInfo.put(session, new ClientInfo(gameId, username));
    }

    public static void broadcastToGame(int gameID, ServerMessage message) {
        broadcastToGame(gameID, message, null);
    }

    public static void broadcastToGame(int gameID, ServerMessage message, Session except) {
        for (Session s : GameSessions.getOrDefault(gameID, Set.of())) {
            if (!s.equals(except)) {
                WebSocketHandler.sendToSession(s, message);
            }
        }
    }


    public static String getUsername(Session session) {
        return SessionToUsername.get(session);
    }
}
