package websocket.commands;


import chess.ChessGame;
public class JoinPlayer extends UserGameCommand {

    private final ChessGame.TeamColor playerColor;

    public JoinPlayer(String authToken, int gameID, ChessGame.TeamColor playerColor) {
        super(CommandType.CONNECT, authToken, gameID); // Or JOIN_PLAYER if you add it to enum
        this.playerColor = playerColor;
    }

    public ChessGame.TeamColor getColor() {
        return playerColor;
    }
}