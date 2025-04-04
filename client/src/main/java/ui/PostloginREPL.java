package ui;

import client.ServerFacade;
import model.GameData;

import java.util.*;

import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class PostloginREPL {

    private final ServerFacade serverFacade;
    private List<GameData> availableGames;

    public PostloginREPL(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
        this.availableGames = new ArrayList<>();
    }

    public void run() {
        boolean sessionActive = true;
        out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);

        while (sessionActive) {
            String[] userCommand = promptUserInput();

            switch (userCommand[0]) {
                case "quit":
                    return;

                case "help":
                    displayHelp();
                    break;

                case "logout":
                    serverFacade.logout();
                    sessionActive = false;
                    break;

                case "list":
                    updateGameList();
                    showGames();
                    break;

                case "create":
                    if (userCommand.length != 2) {
                        out.println("Missing game name.");
                        printCreateUsage();
                        break;
                    }
                    int newGameId = serverFacade.createGame(userCommand[1]);
                    out.printf("Game created with ID: %d%n", newGameId);
                    break;

                case "join":
                    if (userCommand.length != 3) {
                        out.println("Provide a game index and a color.");
                        printJoinUsage();
                        break;
                    }
                    updateGameList();
                    GameData gameToJoin = getGameByIndex(userCommand[1]);
                    if (gameToJoin == null) {
                        out.println("Game not found.");
                        break;
                    }

                    boolean joinSuccess = serverFacade.joinGame(gameToJoin.gameID(), userCommand[2].toUpperCase());
                    if (joinSuccess) {
                        out.println("Successfully joined game.");
                        new BoardPrinter(gameToJoin.game().getBoard()).printBoard();
                    } else {
                        out.println("Join failed: invalid game or color unavailable.");
                        printJoinUsage();
                    }
                    break;

                case "observe":
                    if (userCommand.length != 2) {
                        out.println("Provide a game index to observe.");
                        printObserveUsage();
                        break;
                    }
                    updateGameList();
                    GameData gameToObserve = getGameByIndex(userCommand[1]);
                    if (gameToObserve == null) {
                        out.println("Game not found.");
                        break;
                    }

                    if (serverFacade.joinGame(gameToObserve.gameID(), null)) {
                        out.println("Now observing the game.");
                        new BoardPrinter(gameToObserve.game().getBoard()).printBoard();
                    } else {
                        out.println("Unable to observe: game doesn't exist.");
                        printObserveUsage();
                    }
                    break;

                default:
                    out.println("Unknown command. Type 'help' for a list of valid commands.");
            }
        }

        new PreloginREPL(serverFacade).run();
    }

    private String[] promptUserInput() {
        out.print("\n[LOGGED IN] >>> ");
        return new Scanner(System.in).nextLine().trim().split("\\s+");
    }

    private void updateGameList() {
        availableGames = new ArrayList<>(serverFacade.listGames());
    }

    private void showGames() {
        for (int i = 0; i < availableGames.size(); i++) {
            GameData game = availableGames.get(i);
            String whitePlayer = Optional.ofNullable(game.whiteUsername()).orElse("open");
            String blackPlayer = Optional.ofNullable(game.blackUsername()).orElse("open");

            out.printf("%d -- Game ID: %d | Name: %s | White: %s | Black: %s%n",
                    i, game.gameID(), game.gameName(), whitePlayer, blackPlayer);
        }
    }

    private GameData getGameByIndex(String indexStr) {
        try {
            int index = Integer.parseInt(indexStr);
            if (index < 0 || index >= availableGames.size()) {
                out.println("Invalid game index.");
                return null;
            }
            return availableGames.get(index);
        } catch (NumberFormatException e) {
            out.println("Invalid index format. Must be a number.");
            return null;
        }
    }

    private void displayHelp() {
        printCreateUsage();
        out.println("list - games");
        printJoinUsage();
        printObserveUsage();
        out.println("logout - when you are done");
        out.println("quit - playing chess");
        out.println("help - with possible commands");
    }

    private void printCreateUsage() {
        out.println("create <NAME> - a game");
    }

    private void printJoinUsage() {
        out.println("join <INDEX> [WHITE|BLACK] - a game");
    }

    private void printObserveUsage() {
        out.println("observe <INDEX> - a game");
    }
}
