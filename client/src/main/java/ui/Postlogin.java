package ui;

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

    public Postlogin(Scanner scanner, ServerFacade server, String authToken) {
        this.scanner = scanner;
        this.server = server;
        this.authToken = authToken;
    }

    public void run() {
        System.out.println("You are now logged in. Type 'help' to see available commands.");

        while (true) {
            System.out.print("[LOGGED_IN] >>> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "help" -> printHelp();
                    case "logout" -> {
                        server.logout(authToken);
                        System.out.println("You have been logged out.");
                        return;
                    }
                    case "quit" -> {
                        System.out.println("Goodbye!");
                        System.exit(0);
                    }
                    case "create" -> {
                        if (parts.length < 2) {
                            System.out.println("Usage: create <NAME>");
                        } else {
                            String gameName = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                            server.createGame(gameName, authToken);
                            System.out.println("Game '" + gameName + "' created.");
                        }
                    }
                    case "list" -> handleList();
                    case "join" -> handleJoin(parts);
                    case "observe" -> handleObserve(parts);
                    default -> System.out.println("Unknown command. Type 'help' to see available commands.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printHelp() {
        System.out.println("""
                    === Postlogin Commands ===
                    create <NAME>             - a game
                    list                      - games
                    join <ID> [WHITE|BLACK]   - join a game
                    observe <ID>              - observe a game
                    logout                    - when you are done
                    quit                      - exit the program
                    help                      - with possible commands
                    """);
    }

    private void handleList() throws Exception {
        List<GameData> games = new ArrayList<>(server.listGames(authToken));
        if (games.isEmpty()) {
            System.out.println("No games available.");
            return;
        }

        gameNumberMap.clear();
        System.out.println("Available Games:");
        for (int i = 0; i < games.size(); i++) {
            GameData game = games.get(i);
            gameNumberMap.put(i + 1, game);
            String white = game.whiteUsername() != null ? game.whiteUsername() : "(open)";
            String black = game.blackUsername() != null ? game.blackUsername() : "(open)";
            System.out.printf("%d. %s | White: %s | Black: %s%n", i + 1, game.gameName(), white, black);
        }
    }


    private void handleJoin(String[] parts) throws Exception {
        if (parts.length != 3) {
            System.out.println("Usage: join <ID> [WHITE|BLACK]");
            return;
        }

        int gameNumber;
        try {
            gameNumber = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid game number.");
            return;
        }

        GameData game = gameNumberMap.get(gameNumber);
        if (game == null) {
            System.out.println("Game number not found. Use 'list' to see available games.");
            return;
        }

        ChessGame.TeamColor color;
        try {
            color = ChessGame.TeamColor.valueOf(parts[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid color. Choose WHITE or BLACK.");
            return;
        }

        server.joinGame(game.gameID(), color, authToken);
        System.out.println("Joined game '" + game.gameName() + "' as " + color + ".");
        // Transition to gameplay UI stub
        System.out.println("Drawing board... (gameplay coming soon)");
    }

    private void handleObserve(String[] parts) throws Exception {
        if (parts.length != 2) {
            System.out.println("Usage: observe <ID>");
            return;
        }

        int gameNumber;
        try {
            gameNumber = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid game number.");
            return;
        }

        GameData game = gameNumberMap.get(gameNumber);
        if (game == null) {
            System.out.println("Game number not found. Use 'list' to see available games.");
            return;
        }

        server.observeGame(game.gameID(), authToken);
        System.out.println("Observing game '" + game.gameName() + "'.");
        System.out.println("Drawing board... (observer mode coming soon)");
    }
    }
