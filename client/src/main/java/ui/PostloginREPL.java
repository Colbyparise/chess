package ui;

import client.ServerFacade;
import model.GameData;

import java.util.*;

import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class PostloginREPL {

    private final ServerFacade server;
    private List<GameData> games;

    public PostloginREPL(ServerFacade server) {
        this.server = server;
        this.games = new ArrayList<>();
    }

    public void run() {
        boolean loggedIn = true;
        out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);

        while (loggedIn) {
            String[] input = getUserInput();

            switch (input[0]) {
                case "quit" -> {
                    return;
                }
                case "help" -> printHelpMenu();
                case "logout" -> loggedIn = false;
                case "list" -> {
                    refreshGames();
                    printGames();
                }
                case "create" -> handleCreate(input);
                case "join" -> handleJoin(input);
                case "observe" -> handleObserve(input);
                default -> {
                    out.println("Command not recognized, please try again");
                    printHelpMenu();
                }
            }
        }

        new PreloginREPL(server).run();
    }

    private void handleCreate(String[] input) {
        if (input.length != 2) {
            out.println("Please provide a name");
            printCreate();
            return;
        }

        int gameID = server.createGame(input[1]);
        out.printf("Created game, ID: %d%n", gameID);
    }

    private void handleJoin(String[] input) {
        if (input.length != 3) {
            out.println("Please provide a game ID and color choice");
            printJoin();
            return;
        }

        int gameIndex = Integer.parseInt(input[1]);
        if (gameIndex >= games.size()) {
            out.println("Invalid game ID");
            return;
        }

        GameData game = games.get(gameIndex);
        if (server.joinGame(game.gameID(), input[2].toUpperCase())) {
            out.println("You have joined the game");
            new BoardPrinter(game.game().getBoard()).printBoard();
        } else {
            out.println("Game does not exist or color taken");
            printJoin();
        }
    }

    private void handleObserve(String[] input) {
        if (input.length != 2) {
            out.println("Please provide a game ID");
            printObserve();
            return;
        }

        int gameIndex = Integer.parseInt(input[1]);
        if (gameIndex >= games.size()) {
            out.println("Invalid game ID");
            return;
        }

        GameData game = games.get(gameIndex);
        if (server.joinGame(game.gameID(), null)) {
            out.println("You have joined the game as an observer");
            new BoardPrinter(game.game().getBoard()).printBoard();
        } else {
            out.println("Game does not exist");
            printObserve();
        }
    }

    private String[] getUserInput() {
        out.print("\n[LOGGED IN] >>> ");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().strip().split("\\s+");
    }

    private void refreshGames() {
        games = new ArrayList<>(server.listGames());
    }

    private void printGames() {
        for (int i = 0; i < games.size(); i++) {
            GameData game = games.get(i);
            String whiteUser = Optional.ofNullable(game.whiteUsername()).orElse("open");
            String blackUser = Optional.ofNullable(game.blackUsername()).orElse("open");

            out.printf("%d -- Game Name: %s | White User: %s | Black User: %s%n",
                    i, game.gameName(), whiteUser, blackUser);
        }
    }

    private void printHelpMenu() {
        out.println("Available Commands:");
        printCreate();
        out.println("list - list all games");
        printJoin();
        printObserve();
        out.println("logout - log out of current user");
        out.println("quit - stop playing");
        out.println("help - show this menu");
    }

    private void printCreate() {
        out.println("create <NAME> - create a new game");
    }

    private void printJoin() {
        out.println("join <ID> [WHITE|BLACK] - join a game as color");
    }

    private void printObserve() {
        out.println("observe <ID> - observe a game");
    }
}
