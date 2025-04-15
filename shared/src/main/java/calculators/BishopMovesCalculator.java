package calculators;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class BishopMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> legalMoves = new HashSet<>();
        int startRow = myPosition.getRow();
        int startCol = myPosition.getColumn();
        var piece = board.getPiece(myPosition);

        int[][] directions = {
                {-1, -1}, // down-left
                {-1, 1},  // down-right
                {1, 1},   // up-right
                {1, -1}   // up-left
        };

        for (int[] direction : directions) {
            int rowStep = direction[0];
            int colStep = direction[1];
            int row = startRow + rowStep;
            int col = startCol + colStep;

            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPosition newPos = new ChessPosition(row, col);
                var occupyingPiece = board.getPiece(newPos);

                if (occupyingPiece == null) {
                    legalMoves.add(new ChessMove(myPosition, newPos, null));
                } else {
                    if (occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                        legalMoves.add(new ChessMove(myPosition, newPos, null));
                    }
                    break;
                }

                row += rowStep;
                col += colStep;
            }
        }

        return legalMoves;
    }
}
