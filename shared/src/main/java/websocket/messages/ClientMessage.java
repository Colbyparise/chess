package websocket.messages;

import chess.ChessMove;
import java.util.Objects;

public class ClientMessage {

    public enum ClientMessageType {
        CONNECT,
        MAKE_MOVE,
        RESIGN,
        LEAVE
    }

    private final ClientMessageType commandType;
    private final String authToken;
    private final int gameID;
    private final ChessMove move;

    public ClientMessage(ClientMessageType type, String authToken, int gameID) {
        this(type, authToken, gameID, null);
    }

    public ClientMessage(ClientMessageType type, String authToken, int gameID, ChessMove move) {
        this.commandType = type;
        this.authToken = authToken;
        this.gameID = gameID;
        this.move = move;
    }

    public ClientMessageType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public int getGameID() {
        return gameID;
    }

    public ChessMove getMove() {
        return move;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClientMessage)) {
            return false;
        }
        ClientMessage other = (ClientMessage) obj;
        return gameID == other.gameID &&
                commandType == other.commandType &&
                Objects.equals(authToken, other.authToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandType, authToken, gameID);
    }
}
