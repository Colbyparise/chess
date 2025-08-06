package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMove;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

public class GameCommandProcessor {

    private static final Gson GSON = new Gson();
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
        String username;
        GameData gameData;
        try {
            var auth = authDAO.getAuth(command.getAuthToken());
            if (auth == null) {
                WebSocketHandler.sendToSession(session, new ErrorMessage("Invalid or expired auth token."));
                return;
            }
            username = auth.username();
        } catch (Exception e) {
            WebSocketHandler.sendToSession(session, new ErrorMessage("Invalid auth token."));
            return;
        }

        try {
            gameData = gameDAO.getGame(command.getGameID());
        } catch (Exception e) {
            WebSocketHandler.sendToSession(session, new ErrorMessage("Invalid game ID."));
            return;
        }

        try {
            ClientSessionManager.addToGame(command.getGameID(), session, username);
            ClientSessionManager.registerSession(session, command.getGameID(), username);

            WebSocketHandler.sendToSession(session, new LoadGame(gameData.game()));

            String role = (username.equals(gameData.whiteUsername())) ? "White"
                    : (username.equals(gameData.blackUsername())) ? "Black"
                    : "Observer";
            ClientSessionManager.broadcastToGame(command.getGameID(),
                    new Notification(username + " joined the game as " + role),
                    session);
        } catch (Exception e) {
            WebSocketHandler.sendToSession(session, new ErrorMessage("Failed to complete connection."));
        }
    }


    public void handleMakeMove(UserGameCommand command, Session session) {
            try {
                // Check type safely
                if (!(command instanceof MakeMove moveCommand)) {
                    WebSocketHandler.sendToSession(session, new ErrorMessage("Invalid move command."));
                    return;
                }

                AuthData auth = authDAO.getAuth(command.getAuthToken());
                if (auth == null) {
                    WebSocketHandler.sendToSession(session, new ErrorMessage("Invalid auth token."));
                    return;
                }

                String username = auth.username();
                GameData gameData = gameDAO.getGame(command.getGameID());
                ChessGame game = gameData.game();

                if (game.isGameOver()) {
                    WebSocketHandler.sendToSession(session, new ErrorMessage("The game is already over."));
                    return;
                }

                ChessGame.TeamColor currentTurn = game.getTeamTurn();
                ChessGame.TeamColor playerColor = getPlayerColor(gameData, username);

                if (playerColor == null) {
                    WebSocketHandler.sendToSession(session, new ErrorMessage("You are not a player in this game."));
                    return;
                }

                if (playerColor != currentTurn) {
                    WebSocketHandler.sendToSession(session, new ErrorMessage("It's not your turn."));
                    return;
                }

                ChessMove move = moveCommand.getMove();
                game.makeMove(move);
                gameDAO.updateGame(gameData);


                ServerMessage load = new LoadGame(game);
                ClientSessionManager.broadcastToGame(command.getGameID(), load);

                String moveDesc = String.format("%s moved from %s to %s",
                        username, move.getStartPosition(), move.getEndPosition());

                ClientSessionManager.broadcastToGame(command.getGameID(), new Notification(moveDesc), session);

            } catch (Exception e) {
                WebSocketHandler.sendToSession(session, new ErrorMessage("Error: " + e.getMessage()));
            }
        }


        private ChessGame.TeamColor getPlayerColor(GameData gameData, String username) {
        if (username.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        } else {
            return null;
        }
    }


    public void handleLeave(UserGameCommand command, Session session) {
        try {
            String username = authDAO.getAuth(command.getAuthToken()).username();
            GameData gameData = gameDAO.getGame(command.getGameID());

            ClientSessionManager.removeFromGame(command.getGameID(), session);

            String whiteUsername = gameData.whiteUsername();
            String blackUsername = gameData.blackUsername();

            if (username.equals(whiteUsername)) {
                whiteUsername = null;
            } else if (username.equals(blackUsername)) {
                blackUsername = null;
            }

            // Save updated game data
            gameDAO.updateGame(new GameData(
                    command.getGameID(),
                    whiteUsername,
                    blackUsername,
                    gameData.gameName(),
                    gameData.game()
            ));

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
            if (game.isGameOver()) {
                WebSocketHandler.sendToSession(session, new ErrorMessage("Game is already over."));
                return;
            }

            boolean isWhite = username.equals(gameData.whiteUsername());
            boolean isBlack = username.equals(gameData.blackUsername());

            if (!isWhite && !isBlack) {
                WebSocketHandler.sendToSession(session, new ErrorMessage("Observers cannot resign."));
                return;
            }


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
