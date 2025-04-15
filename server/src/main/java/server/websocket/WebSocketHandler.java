package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.interfaces.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.messages.ClientMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private AuthDAO auth;
    private GameDAO games;
    private Gson gson;

    public WebSocketHandler(AuthDAO auth, GameDAO games) {
        this.auth = auth;
        this.games = games;
        this.gson = new Gson();
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        ClientMessage msg = gson.fromJson(message, ClientMessage.class);
        String username;
        try {
            username = auth.authenticate(msg.getAuthToken()).username();
        } catch (DataAccessException e) {
            sendError(session);
            return;
        }
        var type = msg.getCommandType();
        try {
            switch (type) {
                case ClientMessage.ClientMessageType.CONNECT ->
                        join(username, msg.getAuthToken(), msg.getGameID(), session);

                case ClientMessage.ClientMessageType.LEAVE -> leave(username, msg.getAuthToken(), msg.getGameID());
                case ClientMessage.ClientMessageType.RESIGN ->
                        resign(username, msg.getAuthToken(), msg.getGameID(), session);
                case ClientMessage.ClientMessageType.MAKE_MOVE ->
                        move(username, msg.getAuthToken(), msg.getGameID(), msg.getMove(), session);
            }
        } catch (IOException e) {
            sendError(session);
        }
    }

    private void join(String username, String authToken, int id, Session session) throws IOException {

        try {
            var gameData = games.getGame(new AuthData(authToken, username), id);
            var updateNotif = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null, gameData.game());
            session.getRemote().sendString(gson.toJson(updateNotif));
        } catch (DataAccessException e) {
            throw new IOException(e);
        }

        //finish message
        connections.add(username, id, session);
        var message = String.format("%s connected", username);

        try {
            var game = games.getGame(new AuthData(authToken, username), id);
            if (username.equals(game.whiteUsername())) {
                message += " as the white player";
            }
            else if (username.equals(game.blackUsername())) {
                message += " as the black player";
            }
            else {
                message += " as an observer";
            }

        } catch (DataAccessException e) {
            throw new IOException(e);
        }

        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message, null);
        connections.broadcast(username, id, notification);

        //var response = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Connected");
        //session.getRemote().sendString(gson.toJson(response));
    }

    private void leave(String username, String authToken, int id) throws IOException {
        connections.remove(username);
        var message = String.format("%s disconnected", username);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message, null);
        connections.broadcast(username, id, notification);

        //update db
        try {
            var game = games.getGame(new AuthData(authToken, username), id);
            if (username.equals(game.whiteUsername())) {
                games.updateGame(new GameData(id, null, game.blackUsername(), game.gameName(), game.game()));
            }
            else if (username.equals(game.blackUsername())) {
                games.updateGame(new GameData(id, game.whiteUsername(), null, game.gameName(), game.game()));
            }

            if (connections.getGameConnections(id).isEmpty()) {
                games.deleteGame(id);
            }
        } catch (DataAccessException e) {
            throw new IOException(e);
        }
    }

    private void resign(String username, String authToken, int id, Session session) throws IOException {
        //set the game to not allow moves
        try {
            var gameData = games.getGame(new AuthData(authToken, username), id);
            var chessGame = gameData.game();
            if (chessGame.getCurState() != ChessGame.GameState.IN_PROGRESS) {
                throw new WebsocketException("Error: Game already completed");
            }
            else if (username.equals(gameData.whiteUsername())) {
                chessGame.setCurState(ChessGame.GameState.BLACK_WIN);
            }
            else if (username.equals(gameData.blackUsername())) {
                chessGame.setCurState(ChessGame.GameState.WHITE_WIN);
            }
            else {
                throw new WebsocketException("Error: Observers can't resign!");
            }
            var updatedData = new GameData(id, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), chessGame);
            games.updateGame(updatedData);
            var message = String.format("%s resigned. No more moves may be done on this game", username);
            var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message, null);
            connections.broadcast(null, id, notification);
        } catch (DataAccessException e) {
            throw new IOException(e);
        } catch (WebsocketException e) {
            var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage(), null);
            session.getRemote().sendString(gson.toJson(error));
        }
    }

    private void move(String username, String authToken, int id, ChessMove move, Session session) throws IOException {
        try {
            var gameData = games.getGame(new AuthData(authToken, username), id);
            var chessGame = gameData.game();

            if (chessGame.getCurState() != ChessGame.GameState.IN_PROGRESS) {
                throw new WebsocketException("Error: Game is already finished");
            }
            else if (chessGame.getTeamTurn() == ChessGame.TeamColor.BLACK && username.equals(gameData.blackUsername()) ||
                    chessGame.getTeamTurn() == ChessGame.TeamColor.WHITE && username.equals(gameData.whiteUsername())) {
                chessGame.makeMove(move);
                var updatedData = new GameData(id, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), chessGame);
                games.updateGame(updatedData);
                var load = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null, chessGame);
                connections.broadcast(null, id, load);

                var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                        username + " performed move " + move, null);
                connections.broadcast(username, id, notification);

                if (chessGame.isInCheck(ChessGame.TeamColor.WHITE)) {
                    var checkNotif = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                            "White is in check!", null);
                    connections.broadcast(null, id, checkNotif);
                }
                else if (chessGame.isInCheck(ChessGame.TeamColor.BLACK)) {
                    var checkNotif = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                            "Black is in check!", null);
                    connections.broadcast(null, id, checkNotif);
                }
            }
            else {
                throw new WebsocketException("Error: It is either not your turn or you are an observer");
            }
        } catch (DataAccessException | InvalidMoveException e) {
            var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR,
                    "Error: Invalid move", null);
            session.getRemote().sendString(gson.toJson(error));
        } catch (WebsocketException e) {
            var error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage(), null);
            session.getRemote().sendString(gson.toJson(error));
        }
    }

    private void sendError(Session session) throws IOException {
        var err = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Internal error; try again", null);
        session.getRemote().sendString(gson.toJson(err));
    }
}