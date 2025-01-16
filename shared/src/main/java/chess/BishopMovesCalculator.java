package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        int[][] directions = {
                {-1, -1},
                {-1, 1},
                {1, -1},
                {1, 1}
        };
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
                    if (pieceAtPosition.getTeamColor() != board.getPiece(position).getTeamColor()) {
                        validMoves.add(new ChessMove(position, newPosition, null));
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

