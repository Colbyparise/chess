package ui;

import chess.*;
import client.WebSocketFacade;
import client.ServerFacade;
import client.ServerMessageHandler;
import websocket.commands.MakeMove;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGame;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;
import com.google.gson.Gson;

import java.util.Collection;
import java.util.Scanner;

public class Gameplay implements ServerMessageHandler {
    private final Gson gson = new Gson();
    private final Scanner scanner;
    private final int gameId;
    private final boolean isObserver;
    private final ChessGame.TeamColor playerColor;
    private final String authToken;
    private ChessGame currentGame;
    private final WebSocketFacade sender;
    private final ServerFacade server;
    private final int port;

    public Gameplay(Scanner scanner, ServerFacade server, int gameId, boolean isObserver,
                    ChessGame.TeamColor playerColor, String authToken, int port) {
        this.scanner = scanner;
        this.server = server;
        this.gameId = gameId;
        this.isObserver = isObserver;
        this.playerColor = playerColor;
        this.authToken = authToken;
        this.sender = new WebSocketFacade(this); // Pass this as handler
        this.port = port;
    }

    public void run() {
        sender.connect("ws://localhost:" + port + "/ws",
                new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId));

        while (true) {
            System.out.print("[IN_GAME] >>> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "help" -> printHelp();
                    case "redraw" -> drawBoard();
                    case "move" -> handleMove(parts);
                    case "leave" -> {
                        sender.sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId));
                        System.out.println("You have left the game.");
                        return;
                    }
                    case "resign" -> handleResign();
                    case "highlight" -> handleHighlight(parts);
                    default -> System.out.println("Unknown command. Type 'help' for available commands.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // Called by WebSocketFacade when a message arrives
    @Override
    public void handle(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> handleLoadGame((LoadGame) message);
            case NOTIFICATION -> handleNotification((Notification) message);
            case ERROR -> handleError((ErrorMessage) message);
            default -> System.out.println("Unknown message type: " + message.getServerMessageType());
        }
    }

    // Deserialize JSON into correct subclass before calling handle()
    public void handleMessage(String json) {
        try {
            ServerMessage base = gson.fromJson(json, ServerMessage.class);
            switch (base.getServerMessageType()) {
                case LOAD_GAME -> handle(gson.fromJson(json, LoadGame.class));
                case NOTIFICATION -> handle(gson.fromJson(json, Notification.class));
                case ERROR -> handle(gson.fromJson(json, ErrorMessage.class));
                default -> System.err.println("Unknown server message type: " + base.getServerMessageType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLoadGame(LoadGame message) {
        this.currentGame = message.getGame();

        if (currentGame != null) {
            ChessBoard board = currentGame.getBoard();

            boolean whiteOnBottom;
            if (isObserver) {
                whiteOnBottom = true;
            } else {
                whiteOnBottom = (playerColor == ChessGame.TeamColor.WHITE);
            }

            new ChessBoardDrawer(board).print(whiteOnBottom);
        } else {
            System.out.println("[DEBUG] Received LOAD_GAME with null game state.");
        }
    }


    private void handleNotification(Notification message) {
        System.out.println(message.getMessage());
    }

    private void handleError(ErrorMessage message) {
        System.out.println("Error: " + message.getMessage());
    }

    private void drawBoard() {
        try {
            ChessBoard board = server.getGameBoard(gameId, authToken);
            boolean whiteOnBottom = isObserver || playerColor == ChessGame.TeamColor.WHITE;
            new ChessBoardDrawer(board).print(whiteOnBottom);
        } catch (Exception e) {
            System.out.println("Could not redraw board: " + e.getMessage());
        }
    }

    private void handleMove(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Usage: move <from> <to>");
            return;
        }
        ChessPosition from = parsePosition(parts[1]);
        ChessPosition to = parsePosition(parts[2]);
        if (from == null || to == null) return;
        sender.sendCommand(new MakeMove(authToken, gameId, new ChessMove(from, to, null)));

   }

    private void handleResign() {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
            sender.sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId));
            System.out.println("You have resigned. Game over.");
        } else {
            System.out.println("Resignation cancelled.");
        }
    }

    private void handleHighlight(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Usage: highlight <position>");
            return;
        }
        ChessPosition pos = parsePosition(parts[1]);
        if (pos == null) return;
        try {
            ChessBoard board = server.getGameBoard(gameId, authToken);
            ChessPiece piece = board.getPiece(pos);
            if (piece == null) {
                System.out.println("No piece at that position.");
                return;
            }
            Collection<ChessMove> legalMoves = piece.pieceMoves(board, pos);
            new ChessBoardDrawer(board).printWithHighlights(pos, legalMoves);
        } catch (Exception e) {
            System.out.println("Error fetching board or piece: " + e.getMessage());
        }
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            System.out.println("Invalid position. Use format like e2.");
            return null;
        }
        char col = pos.charAt(0);
        char row = pos.charAt(1);
        if (col < 'a' || col > 'h' || row < '1' || row > '8') {
            System.out.println("Invalid position. Use format like e2.");
            return null;
        }
        return new ChessPosition(row - '0', col - 'a' + 1);
    }

    private void printHelp() {
        System.out.println("""
                help                  - Show this help menu
                redraw                - Redraw the current board
                move <from> <to>      - Move a piece (e.g., move e2 e4)
                highlight <position>  - Highlight legal moves from position
                resign                - Resign the game
                leave                 - Leave the game
                """);
    }
}
