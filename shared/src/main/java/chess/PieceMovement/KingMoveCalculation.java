package chess.piecemovement;
import chess.*;
import java.util.HashSet;

public class KingMoveCalculation implements PieceMovesCalculator {

    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position) {

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
        return PieceMovesCalculator.relativemoves(position, directions, board);
    }
}