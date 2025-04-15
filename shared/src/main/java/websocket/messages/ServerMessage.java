package websocket.messages;

import chess.ChessGame;
import java.util.Objects;

public class ServerMessage {

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    private final ServerMessageType serverMessageType;
    private final String message;
    private final String errorMessage;
    private final ChessGame game;

    public ServerMessage(ServerMessageType type, String message, ChessGame game) {
        this.serverMessageType = type;
        this.game = game;

        if (type == ServerMessageType.ERROR) {
            this.message = null;
            this.errorMessage = message;
        } else {
            this.message = message;
            this.errorMessage = null;
        }
    }

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ChessGame getGame() {
        return game;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ServerMessage)) return false;

        ServerMessage other = (ServerMessage) obj;

        return Objects.equals(serverMessageType, other.serverMessageType) &&
                Objects.equals(message, other.message) &&
                Objects.equals(game, other.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverMessageType, message, game);
    }
}
