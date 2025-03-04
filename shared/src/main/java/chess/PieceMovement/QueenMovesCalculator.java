package chess.piecemovement;
import chess.*;
import java.util.HashSet;

public class QueenMovesCalculator implements PieceMovesCalculator {

    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        int x = position.getColumn();
        int y = position.getRow();

        //possible queen moves
        int[][] directions = {
                {0, 1},
                {1, 0},
                {0, -1},
                {-1, 0},
                {-1, -1},
                {-1, 1},
                {1, -1},
                {1, 1}
        };
        ChessGame.TeamColor team = board.getSquareTeam(position);
        return PieceMovesCalculator.directionalmoves(board, position, directions, y, x, team);
    }
}