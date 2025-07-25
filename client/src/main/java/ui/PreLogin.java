package ui;

import client.ServerFacade;
import model.AuthData;
import model.UserData;
import java.util.Scanner;

public class PreLogin {
    private final Scanner scanner;
    private final ServerFacade serverFacade;

    public PreLogin(Scanner scanner, ServerFacade serverFacade) {
        this.scanner = scanner;
        this.serverFacade = serverFacade;
    }

    public AuthData run() {
        System.out.println("Welcome to 240 Chess, Type 'help' to get started.");

        while (true) {
            System.out.print("[LOGGED_OUT] >>> ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split("\\s+");

            if (parts.length == 0) continue;

            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "help" -> printHelp();
                    case "quit" -> {
                        System.out.println("Goodbye!");
                        return null;
                    }
                    case "login" -> {
                        if (parts.length != 3) {
                            System.out.println("Usage: login <USERNAME> <PASSWORD>");
                        } else {
                            return handleLogin(parts[1], parts[2]);
                        }
                    }
                    case "register" -> {
                        if (parts.length != 4) {
                            System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
                        } else {
                            return handleRegister(parts[1], parts[2], parts[3]);
                        }
                    }
                    default -> System.out.println("Unknown command, type 'help for a list of commands.");
                }
            } catch (Exception exception) {
                System.out.println("Error: " + exception.getMessage());
            }
        }
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
        System.out.println("Login successful, Welcome, " + authdata.username());
        return authData;
    }


    private AuthData handleRegister(String username, String password, String email) throws Exception {
        UserData user = new UserData(username, password, email);
        AuthData auth = server.register(user);
        System.out.println("Registration successful. Welcome, " + auth.username() + "!");
        return auth;
    }
}
