package server.websocket;

import chess.*;
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
    private final AuthDAO auth;
    private final GameDAO games;
    private final Gson gson = new Gson();

    public WebSocketHandler(AuthDAO auth, GameDAO games) {
        this.auth = auth;
        this.games = games;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String rawMsg) throws IOException {
        ClientMessage msg = gson.fromJson(rawMsg, ClientMessage.class);
        String username;

        try {
            username = auth.authenticate(msg.getAuthToken()).username();
        } catch (DataAccessException e) {
            sendError(session);
            return;
        }

        try {
            switch (msg.getCommandType()) {
                case CONNECT -> joinGame(username, msg.getAuthToken(), msg.getGameID(), session);
                case LEAVE -> leaveGame(username, msg.getAuthToken(), msg.getGameID());
                case RESIGN -> resignGame(username, msg.getAuthToken(), msg.getGameID(), session);
                case MAKE_MOVE -> processMove(username, msg.getAuthToken(), msg.getGameID(), msg.getMove(), session);
            }
        } catch (IOException e) {
            sendError(session);
        }
    }

    private void joinGame(String username, String token, int gameId, Session session) throws IOException {
        try {
            var gameData = games.getGame(new AuthData(token, username), gameId);
            session.getRemote().sendString(gson.toJson(new ServerMessage(
                    ServerMessage.ServerMessageType.LOAD_GAME, null, gameData.game())));
        } catch (DataAccessException e) {
            throw new IOException(e);
        }

        connections.add(username, gameId, session);

        String message = username + " connected";
        try {
            var game = games.getGame(new AuthData(token, username), gameId);
            if (username.equals(game.whiteUsername())) message += " as the white player";
            else if (username.equals(game.blackUsername())) message += " as the black player";
            else message += " as an observer";
        } catch (DataAccessException e) {
            throw new IOException(e);
        }

        broadcastNotification(username, gameId, message);
    }

    private void leaveGame(String username, String token, int gameId) throws IOException {
        connections.remove(username);
        broadcastNotification(username, gameId, username + " disconnected");

        try {
            var game = games.getGame(new AuthData(token, username), gameId);
            String white = username.equals(game.whiteUsername()) ? null : game.whiteUsername();
            String black = username.equals(game.blackUsername()) ? null : game.blackUsername();

            games.updateGame(new GameData(gameId, white, black, game.gameName(), game.game()));

            if (connections.getGameConnections(gameId).isEmpty()) {
                games.deleteGame(gameId);
            }
        } catch (DataAccessException e) {
            throw new IOException(e);
        }
    }

    private void resignGame(String username, String token, int gameId, Session session) throws IOException {
        try {
            var gameData = games.getGame(new AuthData(token, username), gameId);
            var game = gameData.game();

            if (game.getCurState() != ChessGame.GameState.IN_PROGRESS) {
                throw new WebsocketException("Error: Game already completed");
            }

            if (username.equals(gameData.whiteUsername())) {
                game.setCurState(ChessGame.GameState.BLACK_WIN);
            } else if (username.equals(gameData.blackUsername())) {
                game.setCurState(ChessGame.GameState.WHITE_WIN);
            } else {
                throw new WebsocketException("Error: Observers can't resign");
            }

            games.updateGame(new GameData(gameId, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game));
            broadcastNotification(null, gameId, username + " resigned. No more moves can be done on this game");

        } catch (DataAccessException e) {
            throw new IOException(e);
        } catch (WebsocketException e) {
            sendMessage(session, new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage(), null));
        }
    }

    private void processMove(String username, String token, int gameId, ChessMove move, Session session) throws IOException {
        try {
            var gameData = games.getGame(new AuthData(token, username), gameId);
            var game = gameData.game();

            if (game.getCurState() != ChessGame.GameState.IN_PROGRESS) {
                throw new WebsocketException("Error: Game is already finished");
            }

            boolean isWhiteTurn = game.getTeamTurn() == ChessGame.TeamColor.WHITE;
            boolean isBlackTurn = game.getTeamTurn() == ChessGame.TeamColor.BLACK;

            if ((isWhiteTurn && username.equals(gameData.whiteUsername())) ||
                    (isBlackTurn && username.equals(gameData.blackUsername()))) {

                game.makeMove(move);
                games.updateGame(new GameData(gameId, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game));

                connections.broadcast(null, gameId, new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null, game));
                broadcastNotification(username, gameId, username + " moved " + move);

                if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
                    broadcastNotification(null, gameId, "White in check!");
                } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
                    broadcastNotification(null, gameId, "Black in check!");
                }

            } else {
                throw new WebsocketException("Error: Not your turn/you are an observer");
            }

        } catch (DataAccessException | InvalidMoveException e) {
            sendMessage(session, new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: Invalid move", null));
        } catch (WebsocketException e) {
            sendMessage(session, new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage(), null));
        }
    }

    private void broadcastNotification(String exceptUser, int gameId, String message) throws IOException {
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message, null);
        connections.broadcast(exceptUser, gameId, notification);
    }

    private void sendMessage(Session session, ServerMessage message) throws IOException {
        session.getRemote().sendString(gson.toJson(message));
    }

    private void sendError(Session session) throws IOException {
        sendMessage(session, new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Internal error; try again", null));
    }
}
