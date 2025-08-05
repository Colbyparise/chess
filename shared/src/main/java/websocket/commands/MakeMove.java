package websocket.commands;

import chess.ChessMove;
import chess.ChessGame;
public class MakeMove extends UserGameCommand {

    private final ChessMove move;

    public MakeMove(String authToken, int gameID, ChessMove move) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }
}