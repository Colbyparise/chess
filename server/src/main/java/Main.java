import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        int port = server.run(0); // 0 means OS picks free port
        System.out.println("♕ 240 Chess Server running on port: " + port);

        // Save the port somewhere accessible to client, e.g. a file or system property
        // For example, write port to a file so client can read it
        try (var writer = new java.io.PrintWriter("server-port.txt")) {
            writer.println(port);
        } catch (Exception e) {
            System.err.println("Failed to write port: " + e.getMessage());
        }
    }
}
