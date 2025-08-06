package ui;

import chess.ChessBoard;
import chess.ChessGame;
import client.ServerFacade;
import model.AuthData;
import model.GameData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.*;


public class Postlogin {
    private final Scanner scanner;
    private final ServerFacade server;
    private final String authToken;
    private final Map<Integer, GameData> gameNumberMap = new HashMap<>();
    private final int port;

    public Postlogin(Scanner scanner, ServerFacade server, String authToken, int port) {
        this.scanner = scanner;
        this.server = server;
        this.authToken = authToken;
        this.port = port;
    }

    public void run() {
        System.out.println("You are now logged in. Type 'help' to see available commands.");

        try {
            while (true) {
                System.out.print("[LOGGED_IN] >>> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {continue; }

                String[] parts = input.split("\\s+");
                String command = parts[0].toLowerCase();

                switch (command) {
                    case "help" -> printHelp();
                    case "logout" -> handleLogout();
                    case "quit" -> handleQuit();
                    case "create" -> handleCreate(parts);
                    case "list" -> handleList();
                    case "join" -> handleJoin(parts);
                    case "observe" -> handleObserve(parts);
                    default -> System.out.println("Unknown command. Type 'help' to see available commands.");
                }
            }
        } catch (ExitPostLogin ignored) {

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleLogout() throws Exception {
        server.logout(authToken);
        System.out.println("You have been logged out.");
        throw new ExitPostLogin();
    }

    private void handleQuit() {
        System.out.println("Goodbye!");
        System.exit(0);
    }

    private void handleCreate(String[] parts) throws Exception {
        if (parts.length < 2) {
            System.out.println("Usage: create <NAME>");
            return;
        }
        String gameName = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        server.createGame(gameName, authToken);
        System.out.println("Game '" + gameName + "' created.");
    }

    private void handleList() throws Exception {
        Set<GameData> games = server.listGames(authToken);
        if (games.isEmpty()) {
            System.out.println("No games available.");
            return;
        }

        gameNumberMap.clear();
        System.out.println("Available Games:");
        int i = 1;
        for (GameData game : games) {
            gameNumberMap.put(i, game);
            String white = game.whiteUsername() != null ? game.whiteUsername() : "(open)";
            String black = game.blackUsername() != null ? game.blackUsername() : "(open)";
            System.out.printf("%d. %s | White: %s | Black: %s%n", i, game.gameName(), white, black);
            i++;
        }
    }

    private void handleJoin(String[] parts) throws Exception {
        if (parts.length != 3) {
            System.out.println("Usage: join <ID> [WHITE|BLACK]");
            return;
        }

        int gameNumber = parseGameNumber(parts[1]);
        if (gameNumber == -1) {
            return;
        }

        GameData game = getGameFromNumber(gameNumber);
        if (game == null) {
            return;
        }

        ChessGame.TeamColor color = parseColor(parts[2]);
        if (color == null) {
            return;
        }

        try {
            server.joinGame(game.gameID(), color, authToken);
            System.out.println("Joined game '" + game.gameName() + "' as " + color + ".");

            ChessBoard board = server.getGameBoard(game.gameID(), authToken);
            boolean whitePerspective = (color == ChessGame.TeamColor.WHITE);
            new ChessBoardDrawer(board).print(whitePerspective);
            Gameplay gameplay = new Gameplay(
                    scanner,
                    server,
                    game.gameID(),
                    false, // not an observer
                    color,
                    authToken,
                    port
            );
            gameplay.run();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    private void handleObserve(String[] parts) throws Exception {
        if (parts.length != 2) {
            System.out.println("Usage: observe <ID>");
            return;
        }

        int gameNumber = parseGameNumber(parts[1]);
        if (gameNumber == -1) {
            return;
        }

        GameData game = getGameFromNumber(gameNumber);
        if (game == null) {
            return;
        }

        server.observeGame(game.gameID(), authToken);
        System.out.println("Observing game '" + game.gameName() + "'.");

        ChessBoard board = server.getGameBoard(game.gameID(), authToken);
        new ChessBoardDrawer(board).print(true); // always white perspective
        Gameplay gameplay = new Gameplay(
                scanner,
                server,
                game.gameID(),
                true, // observer
                null,
                authToken,
                port
        );
        gameplay.run();
    }

    private int parseGameNumber(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid game number.");
            return -1;
        }
    }

    private GameData getGameFromNumber(int number) {
        GameData game = gameNumberMap.get(number);
        if (game == null) {
            System.out.println("Game number not found. Use 'list' to see available games.");
        }
        return game;
    }

    private ChessGame.TeamColor parseColor(String input) {
        try {
            return ChessGame.TeamColor.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid color. Choose WHITE or BLACK.");
            return null;
        }
    }

    private void printHelp() {
        System.out.println("""
                create <NAME>             - a game
                list                      - games
                join <ID> [WHITE|BLACK]   - join a game
                observe <ID>              - observe a game
                logout                    - when you are done
                quit                      - exit the program
                help                      - with possible commands
                """);
    }

    // Use this to exit the loop and return to prelogin
    public static class ExitPostLogin extends RuntimeException {}
}
