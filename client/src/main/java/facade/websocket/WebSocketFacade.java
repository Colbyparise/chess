package facade.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import network.ResponseException;
import ui.CommandEval;
import ui.Printer;
import websocket.messages.ClientMessage;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private Printer printer;
    private Gson gson;

    public WebSocketFacade(String url, Printer printer, CommandEval eval) throws ResponseException {
        try {
            gson = new Gson();
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.printer = printer;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler((MessageHandler.Whole<String>) message -> {
                ServerMessage notification = gson.fromJson(message, ServerMessage.class);
                handleServerMessage(notification, eval);
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        // No action needed on open
    }

    private void handleServerMessage(ServerMessage notification, CommandEval eval) {
        switch (notification.getServerMessageType()) {
            case NOTIFICATION:
                printer.notify(notification.getMessage());
                break;
            case LOAD_GAME:
                eval.loadGame(notification.getGame());
                break;
            default:
                printer.printError(notification.getErrorMessage());
                break;
        }
    }

    private void sendMessage(ClientMessage.ClientMessageType type, String auth, int id, ChessMove move) throws ResponseException {
        try {
            ClientMessage msg = new ClientMessage(type, auth, id, move);
            this.session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void joinGame(String auth, int id) throws ResponseException {
        sendMessage(ClientMessage.ClientMessageType.CONNECT, auth, id, null);
    }

    public void leaveGame(String auth, int id) throws ResponseException {
        sendMessage(ClientMessage.ClientMessageType.LEAVE, auth, id, null);
    }

    public void resign(String auth, int id) throws ResponseException {
        sendMessage(ClientMessage.ClientMessageType.RESIGN, auth, id, null);
    }

    public void move(String auth, int id, ChessMove move) throws ResponseException {
        sendMessage(ClientMessage.ClientMessageType.MAKE_MOVE, auth, id, move);
    }
}
