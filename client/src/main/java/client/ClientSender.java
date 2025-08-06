package client;


import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ClientSender implements Listener {
    private final Gson gson = new Gson();
    private WebSocket webSocket;
    private final ServerMessageHandler handler;
    private UserGameCommand connectCommand;

    public ClientSender(ServerMessageHandler handler) {
        this.handler = handler;
    }

    private final CompletableFuture<Void> connectionReady = new CompletableFuture<>();

    public void connect(String uri, UserGameCommand connectCommand) {
        HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(URI.create(uri), this)
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    System.out.println("Connected to WebSocket");
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
        System.out.println("WebSocket opened");
        connectionReady.complete(null);


        if (connectCommand != null) {
            sendCommand(connectCommand);
            connectCommand = null;
        }
        }


    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        ServerMessage message = gson.fromJson(data.toString(), ServerMessage.class);
        handler.handle(message);
        return Listener.super.onText(webSocket, data, last);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
        Listener.super.onError(webSocket, error);
    }
}