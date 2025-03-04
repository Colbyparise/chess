package chess.piecemovement;
import chess.*;
import java.util.HashSet;

public class KnightMovesCalculator implements PieceMovesCalculator {

    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position) {

        //possible knight moves
        int[][] directions = {
                {2, 1},
                {2, -1},
                {1, 2},
                {1, -2},
                {-2, 1},
                {-2, -1},
                {-1, 2},
                {-1, -2}
        };
        return PieceMovesCalculator.relativemoves(position, directions, board);
    }
}
