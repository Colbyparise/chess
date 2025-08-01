package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletionStage;

public class WebSocketFacade implements Listener {
    private final Gson gson = new Gson();
    private WebSocket webSocket;
    private final ServerMessageHandler handler;

    public WebSocketFacade(ServerMessageHandler handler) {
        this.handler = handler;
    }

    public void connect(String uri) {
        WebSocket.Builder builder = java.net.http.HttpClient.newHttpClient().newWebSocketBuilder();
        builder.buildAsync(URI.create(uri), this).thenAccept(ws -> {
            this.webSocket = ws;
            System.out.println("WebSocket connection established");
        });
    }

    public void sendCommand(UserGameCommand command) {
        String json = gson.toJson(command);
        webSocket.sendText(json, true);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        ServerMessage message = gson.fromJson(data.toString(), ServerMessage.class);
        handler.handle(message); // delegate to gameplay logic/UI
        return Listener.super.onText(webSocket, data, last);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("WebSocket Error: " + error.getMessage());
        Listener.super.onError(webSocket, error);
    }
}
