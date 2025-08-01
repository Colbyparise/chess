package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ServerMessage;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {

    private static final Map<Session, String> sessionToUser = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    private final GameCommandProcessor processor;

    // Constructor with DAOs injected
    public WebSocketHandler(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.processor = new GameCommandProcessor(userDAO, authDAO, gameDAO);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Client connected: " + session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String messageJson) {
        try {
            UserGameCommand baseCommand = gson.fromJson(messageJson, UserGameCommand.class);
            switch (baseCommand.getCommandType()) {
                case CONNECT -> processor.handleConnect(baseCommand, session);
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = gson.fromJson(messageJson, MakeMoveCommand.class);
                    processor.handleMakeMove(moveCommand, session);
                }
                case LEAVE -> processor.handleLeave(baseCommand, session);
                case RESIGN -> processor.handleResign(baseCommand, session);
                default -> sendError(session, "Error: Unknown command type.");
            }
        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + reason);
        sessionToUser.remove(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }

    // Helper method to send error messages
    private void sendError(Session session, String errorMessage) {
        try {
            ServerMessage errorMsg = new websocket.messages.ErrorMessage(errorMessage);
            String json = gson.toJson(errorMsg);
            session.getRemote().sendString(json);
        } catch (IOException e) {
            System.err.println("Error sending error message: " + e.getMessage());
        }
    }

    // You may keep this static if used elsewhere, or make it non-static if you prefer
    public static void sendToSession(Session session, ServerMessage message) {
        try {
            String json = gson.toJson(message);
            session.getRemote().sendString(json);
        } catch (IOException e) {
            System.err.println("Error sending WebSocket message: " + e.getMessage());
        }
    }
}
