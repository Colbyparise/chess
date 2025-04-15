package calculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class QueenMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        var queenMoves = new HashSet<ChessMove>();
        PieceMovesCalculator rookLogic = new RookMovesCalculator();
        queenMoves.addAll(rookLogic.pieceMoves(board, position));

       PieceMovesCalculator bishopLogic = new BishopMovesCalculator();
        queenMoves.addAll(bishopLogic.pieceMoves(board, position));

        return queenMoves;
    }
}
