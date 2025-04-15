package calculators;

import chess.*;

import java.util.Collection;

public interface PieceMovesCalculator {


    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);

    default boolean tryAddGenericMove(
            ChessBoard board,
            ChessMove move,
            Collection<ChessMove> moveSet,
            ChessPiece movePiece
    ) {
        ChessPosition target = move.getEndPosition();
        int col = target.getColumn();
        int row = target.getRow();

        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        }

        ChessPiece destinationPiece = board.getPiece(target);

       if (destinationPiece == null || destinationPiece.getTeamColor() != movePiece.getTeamColor()) {
            moveSet.add(move);
            return true;
        }

        return false;
    }
}
