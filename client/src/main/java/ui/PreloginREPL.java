package ui;

import client.ServerFacade;

import java.util.Scanner;

import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class PreloginREPL {

    private final ServerFacade server;
    private final PostloginREPL postloginREPL;

    public PreloginREPL(ServerFacade server) {
        this.server = server;
        this.postloginREPL = new PostloginREPL(server);
    }

    public void run() {
        boolean loggedIn = false;
        out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
        out.println("Welcome to Chess! Type 'help' for available commands.");

        while (!loggedIn) {
            String[] input = getUserInput();

            switch (input[0].toLowerCase()) {
                case "quit" -> {
                    return;
                }
                case "help" -> {
                    printHelpMenu();
                }
                case "login" -> {
                    if (input.length != 3) {
                        out.println("Please provide a username and password.");
                        printLoginUsage();
                    } else if (server.login(input[1], input[2])) {
                        out.println("You are now logged in.");
                        loggedIn = true;
                    } else {
                        out.println("Invalid username or password. Please try again.");
                        printLoginUsage();
                    }
                }
                case "register" -> {
                    if (input.length != 4) {
                        out.println("Please provide a username, password, and email.");
                        printRegisterUsage();
                    } else if (server.register(input[1], input[2], input[3])) {
                        out.println("Registration successful. You are now logged in.");
                        loggedIn = true;
                    } else {
                        out.println("Username already taken. Please try a different one.");
                        printRegisterUsage();
                    }
                }
                default -> {
                    out.println("Unknown command. Type 'help' for available commands.");
                    printHelpMenu();
                }
            }
        }

        postloginREPL.run();
    }

    private String[] getUserInput() {
        out.print("\n[LOGGED OUT] >>> ");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().split(" ");
    }

    private void printHelpMenu() {
        out.println("\nAvailable Commands:");
        printRegisterUsage();
        printLoginUsage();
        out.println("quit  - Exit the game");
        out.println("help  - Display available commands");
    }

    private void printRegisterUsage() {
        out.println("register <USERNAME> <PASSWORD> <EMAIL>  - Create a new user account");
    }

    private void printLoginUsage() {
        out.println("login <USERNAME> <PASSWORD>  - Log into an existing account");
    }
}
