package server.websocket;

import chess.ChessGame;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ClientSessionManager {
    private static final Map<Integer, Set<Session>> GAME_SESSIONS = new ConcurrentHashMap<>();
    private static final Map<Session, String> SESSION_TO_USERNAME = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, ClientInfo> SESSION_INFO = new ConcurrentHashMap<>();

    public static void addToGame(int gameID, Session session, String username) {
        GAME_SESSIONS.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(session);
        SESSION_TO_USERNAME .put(session, username);
    }

    public static void removeFromGame(int gameID, Session session) {
        Set<Session> sessions = GAME_SESSIONS.getOrDefault(gameID, Set.of());
        sessions.remove(session);
        SESSION_TO_USERNAME .remove(session);
        SESSION_INFO.remove(session);
    }

    public static void registerSession(Session session, int gameId, String username) {
        SESSION_INFO.put(session, new ClientInfo(gameId, username));
    }

    public static void broadcastToGame(int gameID, ServerMessage message) {
        broadcastToGame(gameID, message, null);
    }

    public static void broadcastToGame(int gameID, ServerMessage message, Session except) {
        for (Session s : GAME_SESSIONS.getOrDefault(gameID, Set.of())) {
            if (!s.equals(except)) {
                WebSocketHandler.sendToSession(s, message);
            }
        }
    }


    public static String getUsername(Session session) {
        return SESSION_TO_USERNAME .get(session);
    }
}