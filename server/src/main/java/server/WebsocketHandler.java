package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.messages.Error;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;
import websocket.commands.*;

import java.io.IOException;
import java.util.Objects;
@WebSocket
public class WebsocketHandler {

    @OnWebSocketConnect
    public void onConnect(Session session) {
        Server.gameSessions.put(session, 0);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        Server.gameSessions.remove(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        System.out.printf("Received: %s\n", message);

        if (message.contains("\"commandType\":\"JOIN_PLAYER\"")) {
            Connect command = new Gson().fromJson(message, Connect.class);
            Server.gameSessions.replace(session, command.getGameID());
            handleJoinPlayer(session, command);
        } else if (message.contains("\"commandType\":\"JOIN_OBSERVER\"")) {
            Connect command = new Gson().fromJson(message, Connect.class);
            Server.gameSessions.replace(session, command.getGameID());
            handleJoinObserver(session, command);
        } else if (message.contains("\"commandType\":\"MAKE_MOVE\"")) {
            MakeMove command = new Gson().fromJson(message, MakeMove.class);
            handleMakeMove(session, command);
        } else if (message.contains("\"commandType\":\"LEAVE\"")) {
            Leave command = new Gson().fromJson(message, Leave.class);
            handleLeave(session, command);
        } else if (message.contains("\"commandType\":\"RESIGN\"")) {
            Resign command = new Gson().fromJson(message, Resign.class);
            handleResign(session, command);
        }
    }

    private void handleJoinPlayer(Session session, Connect command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthToken());

            GameData game = Server.liveGames.computeIfAbsent(command.getGameID(), id -> {
                try {
                    return Server.gameService.getGameData(command.getAuthToken(), id);
                } catch (Exception e) {
                    return null;
                }
            });

            if (game == null) {
                sendError(session, new Error("Error: Could not load game"));
                return;
            }

            ChessGame.TeamColor joiningColor = command.getColor().toString().equalsIgnoreCase("white")
                    ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

            boolean correctColor = (joiningColor == ChessGame.TeamColor.WHITE)
                    ? Objects.equals(game.whiteUsername(), auth.username())
                    : Objects.equals(game.blackUsername(), auth.username());

            if (!correctColor) {
                sendError(session, new Error("Error: attempting to join with wrong color"));
                return;
            }

            Notification notif = new Notification("%s has joined the game as %s".formatted(auth.username(), command.getColor()));
            broadcastMessage(session, notif);
            sendMessage(session, new LoadGame(game.game()));

        } catch (UnauthorizedException | BadRequestException e) {
            sendError(session, new Error("Error: " + e.getMessage()));
        }
    }

    private void handleJoinObserver(Session session, Connect command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthToken());
            GameData game = Server.liveGames.computeIfAbsent(command.getGameID(), id -> {
                try {
                    return Server.gameService.getGameData(command.getAuthToken(), id);
                } catch (Exception e) {
                    return null;
                }
            });

            if (game == null) {
                sendError(session, new Error("Error: Could not load game"));
                return;
            }

            Notification notif = new Notification("%s has joined the game as an observer".formatted(auth.username()));
            broadcastMessage(session, notif);
            sendMessage(session, new LoadGame(game.game()));

        } catch (UnauthorizedException | BadRequestException e) {
            sendError(session, new Error("Error: " + e.getMessage()));
        }
    }

    private void handleMakeMove(Session session, MakeMove command) throws IOException {
        try {
            // Step 1: Auth & game
            AuthData auth = Server.userService.getAuth(command.getAuthToken());

            GameData game;
            try {
                game = Server.liveGames.computeIfAbsent(command.getGameID(), id -> {
                    try {
                        return Server.gameService.getGameData(command.getAuthToken(), id);
                    } catch (UnauthorizedException | BadRequestException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (RuntimeException e) {
                if (e.getCause() instanceof UnauthorizedException) {
                    sendError(session, new Error("Error: Not authorized"));
                } else if (e.getCause() instanceof BadRequestException) {
                    sendError(session, new Error("Error: invalid game"));
                } else {
                    sendError(session, new Error("Error: unexpected server error"));
                }
                return;
            }

            // Step 2: Determine user color
            ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);
            if (userColor == null) {
                sendError(session, new Error("Error: You are observing this game"));
                return;
            }

            if (game.game().getGameOver()) {
                sendError(session, new Error("Error: cannot make a move, game is over"));
                return;
            }

            if (!game.game().getTeamTurn().equals(userColor)) {
                sendError(session, new Error("Error: it is not your turn"));
                return;
            }

            // Step 3: Make move
            game.game().makeMove(command.getMove());

            // Step 4: Check game status
            ChessGame.TeamColor opponentColor = userColor == ChessGame.TeamColor.WHITE
                    ? ChessGame.TeamColor.BLACK
                    : ChessGame.TeamColor.WHITE;

            Notification notif;
            if (game.game().isInCheckmate(opponentColor)) {
                notif = new Notification("Checkmate! %s wins!".formatted(auth.username()));
                game.game().setGameOver(true);
            } else if (game.game().isInStalemate(opponentColor)) {
                notif = new Notification("Stalemate caused by %s's move! It's a tie!".formatted(auth.username()));
                game.game().setGameOver(true);
            } else if (game.game().isInCheck(opponentColor)) {
                notif = new Notification("A move has been made by %s, %s is now in check!".formatted(auth.username(), opponentColor));
            } else {
                notif = new Notification("A move has been made by %s".formatted(auth.username()));
            }

            // Step 5: Notify and update
            broadcastMessage(session, notif);
            Server.gameService.updateGame(auth.authToken(), game);
            Server.liveGames.put(command.getGameID(), game); // Refresh cached game
            broadcastMessage(session, new LoadGame(game.game()), true);

        } catch (UnauthorizedException e) {
            sendError(session, new Error("Error: Not authorized"));
        } catch (InvalidMoveException e) {
            sendError(session, new Error("Error: invalid move (you might need to specify a promotion piece)"));
        } catch (BadRequestException e) {
            sendError(session, new Error("Error: invalid game"));
        }
    }

    private void handleLeave(Session session, Leave command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthToken());
            Notification notif = new Notification("%s has left the game".formatted(auth.username()));
            broadcastMessage(session, notif);
            session.close();
        } catch (UnauthorizedException e) {
            sendError(session, new Error("Error: Not authorized"));
        }
    }

    private void handleResign(Session session, Resign command) throws IOException {
        try {
            AuthData auth = Server.userService.getAuth(command.getAuthToken());
            GameData game = Server.liveGames.get(command.getGameID());
            ChessGame.TeamColor userColor = getTeamColor(auth.username(), game);

            if (userColor == null) {
                sendError(session, new Error("Error: You are observing this game"));
                return;
            }

            if (game.game().getGameOver()) {
                sendError(session, new Error("Error: The game is already over!"));
                return;
            }

            String opponentUsername = (userColor == ChessGame.TeamColor.WHITE)
                    ? game.blackUsername() : game.whiteUsername();

            game.game().setGameOver(true);
            Server.gameService.updateGame(auth.authToken(), game);
            Notification notif = new Notification("%s has forfeited, %s wins!".formatted(auth.username(), opponentUsername));
            broadcastMessage(session, notif, true);

        } catch (UnauthorizedException | BadRequestException e) {
            sendError(session, new Error("Error: " + e.getMessage()));
        }
    }

    public void broadcastMessage(Session currSession, ServerMessage message) throws IOException {
        broadcastMessage(currSession, message, false);
    }

    public void broadcastMessage(Session currSession, ServerMessage message, boolean toSelf) throws IOException {
        System.out.printf("Broadcasting (toSelf: %s): %s%n", toSelf, new Gson().toJson(message));
        for (Session session : Server.gameSessions.keySet()) {
            boolean inAGame = Server.gameSessions.get(session) != 0;
            boolean sameGame = Server.gameSessions.get(session).equals(Server.gameSessions.get(currSession));
            boolean isSelf = session == currSession;
            if ((toSelf || !isSelf) && inAGame && sameGame) {
                sendMessage(session, message);
            }
        }
    }

    public void sendMessage(Session session, ServerMessage message) throws IOException {
        session.getRemote().sendString(new Gson().toJson(message));
    }

    private void sendError(Session session, Error error) throws IOException {
        System.out.printf("Error: %s%n", new Gson().toJson(error));
        session.getRemote().sendString(new Gson().toJson(error));
    }

    private ChessGame.TeamColor getTeamColor(String username, GameData game) {
        if (username.equals(game.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        } else if (username.equals(game.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        } else return null;
    }
}
