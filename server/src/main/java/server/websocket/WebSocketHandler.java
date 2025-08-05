package server.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {

    private static final ConcurrentHashMap<Session, String> clients = new ConcurrentHashMap<>();
    private static GameCommandProcessor processor;

    public static void init(GameCommandProcessor commandProcessor) {
        WebSocketHandler.processor = commandProcessor;
    }


    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session.getRemoteAddress());
        clients.put(session, "UNASSIGNED"); // You can map to authToken or gameId
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        Gson gson = new Gson();

        try {
            // Parse just the commandType first
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            String commandType = jsonObject.get("commandType").getAsString();

            UserGameCommand command;

            // Deserialize to the correct subclass
            switch (commandType) {
                case "CONNECT" -> command = gson.fromJson(message, Connect.class);
                case "MAKE_MOVE" -> command = gson.fromJson(message, MakeMove.class);
                case "LEAVE" -> command = gson.fromJson(message, Leave.class);
                case "RESIGN" -> command = gson.fromJson(message, Resign.class);
                default -> throw new IllegalArgumentException("Unknown commandType: " + commandType);
            }

            processor.execute(session, command);

        } catch (Exception e) {
            e.printStackTrace();
            sendToSession(session, new ErrorMessage("Invalid message format"));
        }
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