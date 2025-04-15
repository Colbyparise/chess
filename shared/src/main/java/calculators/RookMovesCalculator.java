package calculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RookMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition currentPos) {
        Set<ChessMove> legalMoves = new HashSet<>();

        int startRow = currentPos.getRow();
        int startCol = currentPos.getColumn();
        var currentPiece = board.getPiece(currentPos);

        int[][] directions = {
                {1, 0},  // Up
                {-1, 0}, // Down
                {0, 1},  // Right
                {0, -1}  // Left
        };

        for (int[] direction : directions) {
            int row = startRow + direction[0];
            int col = startCol + direction[1];

            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPosition nextPos = new ChessPosition(row, col);
                var nextPiece = board.getPiece(nextPos);

                if (nextPiece != null) {
                    if (nextPiece.getTeamColor() != currentPiece.getTeamColor()) {
                        legalMoves.add(new ChessMove(currentPos, nextPos, null));
                    }
                    break;
                } else {
                    legalMoves.add(new ChessMove(currentPos, nextPos, null));
                }

                row += direction[0];
                col += direction[1];
            }
        }

        return legalMoves;
    }
}
