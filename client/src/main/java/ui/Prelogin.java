package ui;

import client.ServerFacade;
import model.AuthData;
import model.UserData;
import java.util.Scanner;


public class Prelogin {
    private final Scanner scanner;
    private final ServerFacade server;

    public Prelogin(Scanner scanner, ServerFacade server) {
        this.scanner = scanner;
        this.server = server;
    }

    public AuthData run() {
        System.out.println("Welcome to 240 Chess, Type 'help' to get started.");

        while (true) {
            System.out.print("[LOGGED_OUT] >>> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                AuthData auth = handleCommand(command, parts);
                if (auth != null) {
                    return auth;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private AuthData handleCommand(String command, String[] parts) throws Exception {
        switch (command) {
            case "help" -> printHelp();
            case "quit" -> {
                System.out.println("Goodbye!");
                return null;
            }
            case "login" -> {
                return attemptLogin(parts);
            }
            case "register" -> {
                return attemptRegister(parts);
            }
            default -> System.out.println("Unknown command, type 'help' for a list of commands.");
        }
        return null;
    }

    private AuthData attemptLogin(String[] parts) throws Exception {
        if (parts.length != 3) {
            System.out.println("Usage: login <USERNAME> <PASSWORD>");
            return null;
        }
        return handleLogin(parts[1], parts[2]);
    }

    private AuthData attemptRegister(String[] parts) throws Exception {
        if (parts.length != 4) {
            System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
            return null;
        }
        return handleRegister(parts[1], parts[2], parts[3]);
    }

    private void printHelp() {
        System.out.println("""
                register <USERNAME> <PASSWORD> <EMAIL>
                login <USERNAME> <PASSWORD>
                quit
                help
                """);
    }

    private AuthData handleLogin(String username, String password) throws Exception {
        AuthData authData = server.login(username, password);
        System.out.println("Login successful, Welcome, " + authData.username());
        return authData;
    }

    private AuthData handleRegister(String username, String password, String email) throws Exception {
        UserData user = new UserData(username, password, email);
        AuthData auth = server.register(user);
        System.out.println("Registration successful. Welcome, " + auth.username() + "!");
        return auth;
    }
}
