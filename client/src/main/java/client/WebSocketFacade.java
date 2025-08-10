package client;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebSocketFacade implements Listener {
    private final Gson gson = new Gson();
    private final ServerMessageHandler handler;
    private WebSocket webSocket;
    private final CompletableFuture<Void> connectionReady = new CompletableFuture<>();

    public WebSocketFacade(ServerMessageHandler handler) {
        this.handler = handler;
    }

    public void connect(String uri, UserGameCommand initialCommand) {
        HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(URI.create(uri), this)
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    connectionReady.complete(null);
                    sendCommand(initialCommand);
                })
                .exceptionally(ex -> {
                    System.err.println("WebSocket connection failed: " + ex.getMessage());
                    connectionReady.completeExceptionally(ex);
                    return null;
                });
    }

    public void sendCommand(UserGameCommand command) {
        if (webSocket == null) {
            System.err.println("Cannot send command — WebSocket not connected.");
            return;
        }
        String json = gson.toJson(command);
        webSocket.sendText(json, true);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        this.webSocket = webSocket;
        connectionReady.complete(null);
        Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            JsonObject jsonObject = JsonParser.parseString(data.toString()).getAsJsonObject();
            String typeStr = jsonObject.get("serverMessageType").getAsString();

            ServerMessage.ServerMessageType type = ServerMessage.ServerMessageType.valueOf(typeStr);

            ServerMessage message;

            switch (type) {
                case LOAD_GAME -> message = gson.fromJson(data.toString(), LoadGame.class);
                case NOTIFICATION -> message = gson.fromJson(data.toString(), Notification.class);
                case ERROR -> message = gson.fromJson(data.toString(), ErrorMessage.class);
                default -> throw new IllegalArgumentException("Unknown message type: " + typeStr);
            }

            handler.handle(message);

        } catch (Exception e) {
            System.err.println("Error parsing server message: " + e.getMessage());
        }
        return Listener.super.onText(webSocket, data, last);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
        Listener.super.onError(webSocket, error);
    }
}
