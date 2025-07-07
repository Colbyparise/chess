package chess;

import java.util.HashSet;

public class KnightMovesCalculator implements PieceMovesCalculator{

    public static HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();

        int[][] moves = {
                {1, 2},
                {2, 1},
                {-1, 2},
                {-2, 1},
                {-1, -2},
                {1, -2},
                {2, -1},
                {-2, -1}

        };;
        return PieceMovesCalculator.staticMoves(position, moves, board);
    }
}