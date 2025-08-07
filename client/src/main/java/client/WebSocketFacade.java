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
    private WebSocket webSocket;
    private final ServerMessageHandler handler;
    private UserGameCommand connectCommand;

    public WebSocketFacade(ServerMessageHandler handler) {
        this.handler = handler;
    }

    private final CompletableFuture<Void> connectionReady = new CompletableFuture<>();

    public void connect(String uri, UserGameCommand connectCommand) {
        HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(URI.create(uri), this)
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    connectionReady.complete(null);

                    sendCommand(connectCommand);
                })
                .exceptionally(ex -> {
                    System.err.println("WebSocket connection failed: " + ex.getMessage());
                    connectionReady.completeExceptionally(ex);
                    return null;
                });
    }


    public void sendCommand(UserGameCommand command) {
        if (webSocket == null) {
            System.err.println("WebSocket not connected.");
            return;
        }
        String json = gson.toJson(command);
        webSocket.sendText(json, true);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        this.webSocket = webSocket;
        connectionReady.complete(null);


        if (connectCommand != null) {
            sendCommand(connectCommand);
            connectCommand = null;
        }
        }


    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String json = data.toString();

        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            String type = jsonObject.get("serverMessageType").getAsString();

            ServerMessage message = switch (type) {
                case "LOAD_GAME" -> gson.fromJson(json, LoadGame.class);
                case "ERROR" -> gson.fromJson(json, ErrorMessage.class);
                case "NOTIFICATION" -> gson.fromJson(json, Notification.class);
                default -> {
                    System.err.println("Unknown message type: " + type);
                    yield null;
                }
            };

            if (message != null) {
                handler.handle(message);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse WebSocket message: " + e.getMessage());
        }

        return Listener.super.onText(webSocket, data, last);
    }


    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
        Listener.super.onError(webSocket, error);
    }
}