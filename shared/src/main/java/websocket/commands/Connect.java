package websocket.commands;
import chess.ChessGame;

public class Connect extends UserGameCommand {
    private String color;

    // For JOIN_PLAYER
    public Connect(String authToken, int gameID, ChessGame.TeamColor color) {
        super(CommandType.CONNECT, authToken, gameID);
        this.color = color.toString(); // store as String
    }

    // For JOIN_OBSERVER (no color)
    public Connect(String authToken, int gameID) {
        super(CommandType.CONNECT, authToken, gameID);
        this.color = null;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
