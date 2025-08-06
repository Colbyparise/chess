package chess;

import java.util.Set;

public interface PieceMovesCalculator {
    Set<ChessMove> calculateMoves(ChessBoard board, ChessPosition from);
}
