package chess;

import java.util.HashSet;

public class KingMovesCalculator implements PieceMovesCalculator{

    public static HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();

        int[][] moves = {
                {1, -1},
                {1, 1},
                {-1, 1},
                {-1, -1},
                {0, 1},
                {0, -1},
                {1, 0},
                {-1, 0}
        };
        ChessGame.TeamColor team = board.getColor(position);
        return PieceMovesCalculator.StaticMoves(position, moves, board);
    }
}