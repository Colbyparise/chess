package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import client.ServerFacade;

import java.util.Collection;
import java.util.Scanner;

public class Gameplay {
    private final Scanner scanner;
    private final ServerFacade server;
    private final int gameId;
    private final boolean isObserver;
    private final ChessGame.TeamColor playerColor;
    private final String authToken;

    public Gameplay(Scanner scanner, ServerFacade server, int gameId, boolean isObserver, ChessGame.TeamColor playerColor, String authToken) {
        this.scanner = scanner;
        this.server = server;
        this.gameId = gameId;
        this.isObserver = isObserver;
        this.playerColor = playerColor;
        this.authToken = authToken;
    }

    public void run() {
        System.out.println("You are now in game " + gameId + ". Type 'help' for a list of commands.");
        drawBoard();

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
                        server.leaveGame(gameId, authToken);
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

    private void drawBoard() {
        try {
            ChessBoard board = server.getGameBoard(gameId, authToken);
            boolean whiteOnBottom = isObserver || playerColor == ChessGame.TeamColor.WHITE;
            new ChessBoardDrawer(board).print(whiteOnBottom);
        } catch (Exception e) {
            System.out.println("Unable to draw board: " + e.getMessage());
        }
    }

    private void handleMove(String[] parts) throws Exception {
        if (parts.length != 3) {
            System.out.println("Usage: move <from> <to> (e.g., move e2 e4)");
            return;
        }

        ChessPosition from = parsePosition(parts[1]);
        ChessPosition to = parsePosition(parts[2]);

        if (from == null || to == null) return;

        ChessMove move = new ChessMove(from, to, null); // Add promotion logic if needed
        server.makeMove(gameId, authToken, move);
        System.out.println("Move made: " + parts[1] + " to " + parts[2]);
        drawBoard();
    }

    private void handleResign() throws Exception {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (confirmation.equals("yes")) {
            server.resignGame(gameId, authToken);
            System.out.println("You have resigned. Game over.");
        } else {
            System.out.println("Resignation cancelled.");
        }
    }

    private void handleHighlight(String[] parts) throws Exception {
        if (parts.length != 2) {
            System.out.println("Usage: highlight <position> (e.g., highlight e2)");
            return;
        }

        ChessPosition pos = parsePosition(parts[1]);
        if (pos == null) return;

        ChessBoard board = server.getGameBoard(gameId, authToken);
        Collection<ChessMove> legalMoves = board.getPiece(pos).pieceMoves(board, pos);
        new ChessBoardDrawer(board).printWithHighlights(pos, legalMoves);
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
                highlight <position>  - Highlight legal moves from position (e.g., highlight e2)
                resign                - Resign the game
                leave                 - Leave the game and return to the menu
                """);
    }

}
