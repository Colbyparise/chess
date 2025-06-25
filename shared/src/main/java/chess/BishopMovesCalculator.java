package chess;

import java.util.HashSet;

public class BishopMovesCalculator implements PieceMovesCalculator {

    @Override
    public HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();

        int[][] moves = {
                {1, -1},
                {1, 1},
                {-1, 1},
                {-1, -1}
        };
        return PieceMovesCalculator.horizontalMoves(board, position, row, col, color);
    }

}
