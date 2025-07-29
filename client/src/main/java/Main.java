import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;
import client.ServerFacade;
import ui.Prelogin;
import ui.Postlogin;

public class Main {
    public static void main(String[] args) {
        System.out.println("♕ 240 Chess Client: ");

        int port = 8080; // fallback port
        try {
            String portStr = Files.readString(Paths.get("server-port.txt")).trim();
            port = Integer.parseInt(portStr);
        } catch (Exception e) {
            System.err.println("Failed to read port from file, using default " + port);
        }

        Scanner scanner = new Scanner(System.in);
        ServerFacade server = new ServerFacade(port);

        while (true) {
            Prelogin prelogin = new Prelogin(scanner, server);
            var auth = prelogin.run();

            if (auth == null) {
                // User chose to quit
                break;
            }

            Postlogin postlogin = new Postlogin(scanner, server, auth.authToken());
            postlogin.run(); // this handles logout or quit
        }

        System.out.println("Exited");
    }
}