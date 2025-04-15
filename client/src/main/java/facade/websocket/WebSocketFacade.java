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

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    Session session;
    Printer printer;
    Gson gson;

    public WebSocketFacade(String url, Printer printer, CommandEval eval) throws ResponseException {
        try {
            gson = new Gson();

            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.printer = printer;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
                    switch (notification.getServerMessageType()) {
                        case NOTIFICATION -> printer.notify(notification.getMessage());
                        case LOAD_GAME -> eval.loadGame(notification.getGame());
                        default -> printer.printError(notification.getErrorMessage());
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void joinGame(String auth, int id) throws ResponseException {
        try {
            var msg = new ClientMessage(ClientMessage.ClientMessageType.CONNECT, auth, id);
            this.session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void leaveGame(String auth, int id) throws ResponseException {
        try {
            var msg = new ClientMessage(ClientMessage.ClientMessageType.LEAVE, auth, id);
            this.session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void resign(String auth, int id) throws ResponseException {
        try {
            var msg = new ClientMessage(ClientMessage.ClientMessageType.RESIGN, auth, id);
            this.session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void move(String auth, int id, ChessMove move) throws ResponseException {
        try {
            var msg = new ClientMessage(ClientMessage.ClientMessageType.MAKE_MOVE, auth, id, move);
            this.session.getBasicRemote().sendText(gson.toJson(msg));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


}