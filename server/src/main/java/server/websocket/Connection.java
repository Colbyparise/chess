package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;

public class Connection {
    public final String username;
    public final int gameId;
    public final Session session;

    public Connection(String username, int gameId, Session session) {
        this.username = username;
        this.gameId = gameId;
        this.session = session;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}