package ui;

import client.ServerFacade;

import java.util.Scanner;

import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class PreloginREPL {

    private final ServerFacade serverFacade;
    private final PostloginREPL postLoginUI;

    public PreloginREPL(ServerFacade server) {
        this.serverFacade = server;
        this.postLoginUI = new PostloginREPL(server);
    }

    public void run() {
        boolean isAuthenticated = false;
        out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
        out.println("Welcome to 240 chess. Type help to get started.");

        while (!isAuthenticated) {
            String[] commandArgs = promptInput();
            String command = commandArgs[0];

            switch (command) {
                case "quit":
                    return;

                case "help":
                    showHelpMenu();
                    break;

                case "login":
                    if (commandArgs.length != 3) {
                        out.println("Missing arguments: username and password required.");
                        showLoginUsage();
                        break;
                    }
                    if (serverFacade.login(commandArgs[1], commandArgs[2])) {
                        out.println("Login successful!");
                        isAuthenticated = true;
                    } else {
                        out.println("Login failed: invalid credentials.");
                        showLoginUsage();
                    }
                    break;

                case "register":
                    if (commandArgs.length != 4) {
                        out.println("Missing arguments: username, password, and email required.");
                        showRegisterUsage();
                        break;
                    }
                    if (serverFacade.register(commandArgs[1], commandArgs[2], commandArgs[3])) {
                        out.println("Registration successful! You are now logged in.");
                        isAuthenticated = true;
                    } else {
                        out.println("Username already taken. Please choose a different one.");
                        showRegisterUsage();
                    }
                    break;

                default:
                    out.println("Unknown command.");
                    showHelpMenu();
                    break;
            }
        }

        postLoginUI.run();
    }

    private String[] promptInput() {
        out.print("\n[LOGGED OUT] >>> ");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().trim().split(" ");
    }

    private void showHelpMenu() {
        showRegisterUsage();
        showLoginUsage();
        out.println("quit - playing chess");
        out.println("help - with possible commands");
    }

    private void showRegisterUsage() {
        out.println("register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
    }

    private void showLoginUsage() {
        out.println("login <USERNAME> <PASSWORD> - to play chess");
    }
}
