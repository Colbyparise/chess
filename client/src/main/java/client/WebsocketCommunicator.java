package client;

import chess.ChessGame;
import com.google.gson.Gson;
import ui.GameplayREPL;
import websocket.messages.Error;
import websocket.messages.LoadGame;
import websocket.messages.Notification;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static ui.EscapeSequences.ERASE_LINE;

public class WebsocketCommunicator extends Endpoint {

    Session session;

    public WebsocketCommunicator(String serverDomain) throws Exception {
        try {
            URI uri = new URI("ws://" + serverDomain + "/connect");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, uri);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }
            });

        } catch (DeploymentException | IOException | URISyntaxException ex) {
            System.err.println("Error creating WebSocket connection: " + ex.getMessage()); // Log the error
            ex.printStackTrace(); // Log the stack trace
            throw new WebsocketException("Failed to create WebSocket connection", ex); // Or re-throw the original
        }

    }

    // Custom exception (optional)
    public static class WebsocketException extends Exception {
        public WebsocketException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }

    private void handleMessage(String message) {
        if (message.contains("\"serverMessageType\":\"NOTIFICATION\"")) {
            Notification notif = new Gson().fromJson(message, Notification.class);
            printNotification(notif.getMessage());
        } else if (message.contains("\"serverMessageType\":\"ERROR\"")) {
            Error error = new Gson().fromJson(message, Error.class);
            printNotification(error.getMessage());
        } else if (message.contains("\"serverMessageType\":\"LOAD_GAME\"")) {
            LoadGame loadGame = new Gson().fromJson(message, LoadGame.class);
            printLoadedGame(loadGame.getGame());
        }
    }

    private void printNotification(String message) {
        System.out.print(ERASE_LINE + '\r');
        System.out.printf("\n%s\n[IN-GAME] >>> ", message);
    }

    private void printLoadedGame(ChessGame game) {
        System.out.print(ERASE_LINE + "\r\n");
        GameplayREPL.boardPrinter.updateGame(game);
        GameplayREPL.boardPrinter.printBoard(GameplayREPL.color, null);
        System.out.print("[IN-GAME] >>> ");
    }

    public void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            this.session.getAsyncRemote().sendText(message);
        } else {
            System.err.println("WebSocket session is not open or is null.  Cannot send message.");
            // Consider throwing an exception or returning an error code here,
            // depending on how you want to handle this situation.
        }
    }
}