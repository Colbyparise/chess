package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMovesCalculator implements PieceMovesCalculator  {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> validMoves = new ArrayList<>();

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
        ChessPiece currentPiece = board.getPiece(position);
        for(int[] direction : directions) {
            int row = position.getRow();
            int col = position.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];

                if (!isValidPosition(row, col)) {
                    break;
                }
                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece pieceAtPosition = board.getPiece(newPosition);

                if (pieceAtPosition == null) {
                    validMoves.add(new ChessMove(position, newPosition, null));

                } else {
                    if (pieceAtPosition.getTeamColor() != currentPiece.getTeamColor()) {
                        validMoves.add(new ChessMove(position, newPosition, pieceAtPosition.getPieceType()));
                    }
                    break;
                }

            }
        }
        return validMoves;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}

