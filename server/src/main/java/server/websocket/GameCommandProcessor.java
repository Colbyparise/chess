package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMove;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

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

    public void execute(Session session, UserGameCommand command) {
        try {
            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(command, session);
                case MAKE_MOVE -> handleMakeMove((MakeMove) command, session);
                case LEAVE -> handleLeave(command, session);
                case RESIGN -> handleResign(command, session);
                default -> WebSocketHandler.sendToSession(session, new ErrorMessage("Unknown command type."));
            }
        } catch (Exception e) {
            WebSocketHandler.sendToSession(session, new ErrorMessage("Command execution error: " + e.getMessage()));
        }
    }

    public void handleConnect(UserGameCommand command, Session session) {
        try {
            String username = authDAO.getAuth(command.getAuthToken()).username();
            GameData gameData = gameDAO.getGame(command.getGameID());

            ClientSessionManager.addToGame(command.getGameID(), session, username);
            ClientSessionManager.registerSession(session, command.getGameID(), username);

            ServerMessage load = new LoadGame(gameData.game());
            WebSocketHandler.sendToSession(session, load);

            String role = (username.equals(gameData.whiteUsername())) ? "White"
                    : (username.equals(gameData.blackUsername())) ? "Black"
                    : "Observer";
            ServerMessage notify = new Notification(username + " joined the game as " + role);
            ClientSessionManager.broadcastToGame(command.getGameID(), notify, session);

        } catch (Exception e) {
            WebSocketHandler.sendToSession(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    public void handleMakeMove(MakeMove command, Session session) {
        try {
            String username = authDAO.getAuth(command.getAuthToken()).username();
            GameData gameData = gameDAO.getGame(command.getGameID());


            ChessGame game = gameData.game();
            ChessMove move = command.getMove();

            game.makeMove(move);

            gameDAO.updateGame(new GameData(
                    command.getGameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            ));

            ServerMessage load = new LoadGame(game);
            ClientSessionManager.broadcastToGame(command.getGameID(), load);

            Notification notification = new Notification(
                    username + " moved from " + move.getStartPosition() + " to " + move.getEndPosition()
            );

            // Send notification to all EXCEPT the player who made the move
            ClientSessionManager.broadcastToGameExcept(command.getGameID(), username, notification);

            ChessGame.TeamColor opponent = (game.getTeamTurn() == ChessGame.TeamColor.WHITE)
                    ? ChessGame.TeamColor.BLACK
                    : ChessGame.TeamColor.WHITE;

            if (game.isInCheckmate(opponent)) {
                ClientSessionManager.broadcastToGame(command.getGameID(), new Notification("Checkmate!"));
            } else if (game.isInCheck(opponent)) {
                ClientSessionManager.broadcastToGame(command.getGameID(), new Notification("Check!"));
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
            ClientSessionManager.broadcastToGame(command.getGameID(), new Notification(msg), session);

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
                    command.getGameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            ));

            String msg = username + " has resigned.";
            ClientSessionManager.broadcastToGame(command.getGameID(), new Notification(msg));

        } catch (Exception e) {
            WebSocketHandler.sendToSession(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }
}
