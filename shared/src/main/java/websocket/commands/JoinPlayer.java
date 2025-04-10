package websocket.commands;

import chess.ChessGame;

public class JoinPlayer extends UserGameCommand {

    private final ChessGame.TeamColor color;

    public JoinPlayer(String authToken, int gameID, ChessGame.TeamColor color) {
        super(CommandType.CONNECT, authToken, gameID); // CONNECT is likely used for JOINs
        this.color = color;
    }

    public ChessGame.TeamColor getColor() {
        return color;
    }

    public String getAuthString() {
        return getAuthToken(); // reuse the getter from UserGameCommand
    }
}