package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {

    private static final ConcurrentHashMap<Session, String> clients = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session.getRemoteAddress());
        clients.put(session, "UNASSIGNED"); // You can map to authToken or gameId
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println("Received from client: " + message);
        session.getRemote().sendString("{\"message\":\"Hello from server\"}");
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + reason);
        clients.remove(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }
    public static void sendToSession(Session session, ServerMessage message) {
        try {
            String json = new Gson().toJson(message);
            session.getRemote().sendString(json);
        } catch (IOException e) {
            System.err.println("Failed to send message to session: " + e.getMessage());
        }
    }
}
