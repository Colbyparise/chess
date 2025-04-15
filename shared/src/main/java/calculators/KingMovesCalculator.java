package calculators;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class KingMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> legalMoves = new HashSet<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        var kingPiece = board.getPiece(myPosition);

        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int colOffset = -1; colOffset <= 1; colOffset++) {
                if (rowOffset == 0 && colOffset == 0) {
                    continue;
                }
                int targetRow = row + rowOffset;
                int targetCol = col + colOffset;
                ChessPosition destination = new ChessPosition(targetRow, targetCol);
                ChessMove move = new ChessMove(myPosition, destination, null);

                tryAddGenericMove(board, move, legalMoves, kingPiece);
            }
        }

        return legalMoves;
    }
}
