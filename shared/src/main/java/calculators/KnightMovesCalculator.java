package calculators;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class KnightMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> legalMoves = new HashSet<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        var knight = board.getPiece(myPosition);

        int[][] moveOffsets = {
                {2, 1}, {-2, 1}, {2, -1}, {-2, -1},
                {1, 2}, {-1, 2}, {1, -2}, {-1, -2}
        };

        for (int[] offset : moveOffsets) {
            int targetRow = row + offset[0];
            int targetCol = col + offset[1];
            ChessPosition destination = new ChessPosition(targetRow, targetCol);
            ChessMove potentialMove = new ChessMove(myPosition, destination, null);

            tryAddGenericMove(board, potentialMove, legalMoves, knight);
        }

        return legalMoves;
    }
}
