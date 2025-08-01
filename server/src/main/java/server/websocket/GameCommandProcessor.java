package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.*;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import org.eclipse.jetty.websocket.api.Session;

public class GameCommandProcessor {
    private static final Gson gson = new Gson();
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameCommandProcessor(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void handleConnect(UserGameCommand command, Session session) {
        try {
            String username = authDAO.getAuth(command.getAuthToken()).username();
            GameData gameData = gameDAO.getGame(command.getGameID());

            ClientSessionManager.addToGame(command.getGameID(), session, username);

            // Send full game state back to new client
            ServerMessage load = new LoadGameMessage(gameData.game());
            WebSocketHandler.sendToSession(session, load);

            // Notify others
            String role = (gameData.whiteUsername().equals(username)) ? "White"
                    : (gameData.blackUsername().equals(username)) ? "Black"
                    : "Observer";
            ServerMessage notify = new NotificationMessage(username + " joined the game as " + role);
            ClientSessionManager.broadcastToGame(command.getGameID(), notify, session);
        } catch (Exception e) {
            WebSocketHandler.sendToSession(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    public void handleMakeMove(MakeMoveCommand command, Session session) {
        try {
            String username = authDAO.getAuth(command.getAuthToken()).username();
            GameData gameData = gameDAO.getGame(command.getGameID());

            ChessGame game = gameData.game();
            ChessMove move = command.getMove();

            game.makeMove(move);
            gameDAO.updateGame(new GameData(
                    command.getGameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game));

            ServerMessage load = new LoadGameMessage(game);
            ClientSessionManager.broadcastToGame(command.getGameID(), load);

            String msg = username + " moved from " + move.getStartPosition() + " to " + move.getEndPosition();
            ClientSessionManager.broadcastToGame(command.getGameID(), new NotificationMessage(msg));

            if (game.isInCheckmate(game.getTeamTurn())) {
                ClientSessionManager.broadcastToGame(command.getGameID(), new NotificationMessage("Checkmate!"));
            } else if (game.isInCheck(game.getTeamTurn())) {
                ClientSessionManager.broadcastToGame(command.getGameID(), new NotificationMessage("Check!"));
            }

        } catch (Exception e) {
            WebSocketHandler.sendToSession(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    public void handleLeave(UserGameCommand command, Session session) {
        try {
            String username = authDAO.getAuth(command.getAuthToken()).username();
            ClientSessionManager.removeFromGame(command.getGameID(), session);

            String msg = username + " left the game.";
            ClientSessionManager.broadcastToGame(command.getGameID(), new NotificationMessage(msg), session);
        } catch (Exception e) {
            WebSocketHandler.sendToSession(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    public void handleResign(UserGameCommand command, Session session) {
        try {
            String username = authDAO.getAuth(command.getAuthToken()).username();
            GameData gameData = gameDAO.getGame(command.getGameID());

            ChessGame game = gameData.game();
            game.setGameOver(true);

            gameDAO.updateGame(new GameData(
                    command.getGameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game));

            String msg = username + " has resigned.";
            ClientSessionManager.broadcastToGame(command.getGameID(), new NotificationMessage(msg));
        } catch (Exception e) {
            WebSocketHandler.sendToSession(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }
}
